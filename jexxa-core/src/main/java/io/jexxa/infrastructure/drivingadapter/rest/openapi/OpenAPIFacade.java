package io.jexxa.infrastructure.drivingadapter.rest.openapi;


import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING;
import static io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter.OPEN_API_PATH;
import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.javalin.core.JavalinConfig;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.dsl.DocumentedContent;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.jexxa.utils.JexxaLogger;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

@SuppressWarnings("java:S1602") // required to avoid ambiguous warnings
public class OpenAPIFacade
{
    private final Properties properties;
    private final JavalinConfig javalinConfig;
    private OpenApiOptions openApiOptions;

    public OpenAPIFacade(Properties properties, JavalinConfig javalinConfig)
    {
        this.properties = properties;
        this.javalinConfig = javalinConfig;
        initOpenAPI();
    }
    private void initOpenAPI()
    {
        if (properties.containsKey(OPEN_API_PATH))
        {
            Info applicationInfo = new Info()
                    .version("1.0")
                    .description(properties.getProperty("io.jexxa.context.name", "Unknown Context"))
                    .title(properties.getProperty("io.jexxa.context.name", "Unknown Context"));

            openApiOptions = new OpenApiOptions(applicationInfo)
                    .path("/" + properties.getProperty(OPEN_API_PATH));

            javalinConfig.registerPlugin(new OpenApiPlugin(openApiOptions));
            javalinConfig.enableCorsForAllOrigins();

            openApiOptions.defaultDocumentation(doc -> {
                doc.json("400", BadRequestResponse.class);
            });
        }
    }

    public void documentGET(Method method, String resourcePath)
    {
        if ( openApiOptions != null )
        {
            var openApiDocumentation = OpenApiBuilder
                    .document()
                    .operation(openApiOperation -> {
                        openApiOperation.operationId(method.getName());
                    })
                    .json("200", method.getReturnType());
            openApiOptions.setDocumentation(resourcePath, HttpMethod.GET, openApiDocumentation);
        }
    }

    public void documentPOST(Method method, String resourcePath)
    {
        if ( openApiOptions != null )
        {
            var openApiDocumentation = OpenApiBuilder
                    .document()
                    .operation(openApiOperation -> {
                        openApiOperation.operationId(method.getName());
                    });

            if (method.getParameters().length == 1 )
            {
                openApiDocumentation.body(method.getParameters()[0].getType());
            }  else if ( method.getParameters().length > 1 )
            {
                var schema = new ComposedSchema();
                var arguments = new Object[method.getParameterTypes().length];

                var documentedObjects = new ArrayList<DocumentedContent>();

                for (int i = 0; i < method.getParameterTypes().length; ++i)
                {
                    arguments[i] = createObject(method.getParameterTypes()[i]);

                    documentedObjects.add(new DocumentedContent(method.getParameterTypes()[i]));

                    schema.addAnyOfItem(createSchema(method.getParameterTypes()[i]));
                }

                schema.setExample(arguments);

                openApiDocumentation.body(documentedObjects);
                openApiDocumentation.body(schema, "application/json");
            }

            if ( method.getReturnType() != void.class )
            {
                openApiDocumentation.json("200", method.getReturnType());
            }
            else {
                openApiDocumentation.result("200");
            }

            openApiOptions.setDocumentation(resourcePath, HttpMethod.POST, openApiDocumentation);
        }
    }


    private String createSchemaAsString(Class<?> clazz) throws JsonProcessingException
    {
        var mapper = new ObjectMapper();
        //There are other configuration options you can set.  This is the one I needed.
        mapper.configure(WRITE_ENUMS_USING_TO_STRING, true);

        JsonSchema schema = mapper.generateJsonSchema(clazz);

        System.out.println(schema.toString());

        return schema.toString();
    }


    private Object createObject(Class<?> clazz)
    {
        try
        {
            var schemaString = createSchemaAsString(clazz);
            JsonElement element = JsonParser.parseString(schemaString);
            Gson gson = new Gson();
            if (!schemaString.contains("\"type\":\"object\""))
            {
                System.out.println("here PRIMITIVE : " + clazz.getSimpleName());
                //TODO replace with static mapping to all primitives
                return primitiveToWrapper(clazz).getConstructor(clazz).newInstance(0);
            }
            if (element.isJsonObject())
            {
                System.out.println("here OBJECT : " + clazz.getSimpleName());
                return gson.fromJson(element, clazz);
            }
        } catch (Exception e) {
            JexxaLogger.getLogger(getClass()).warn( "Could not create Object {}" , clazz.getName() , e );
        }
        return null;
    }

    private Schema<?> createSchema(Class<?> clazz)
    {
        if (clazz.equals( Integer.class ) ||
            clazz.equals( int.class ))
        {
            return new IntegerSchema();
        }

        var result = new ObjectSchema();
        result.set$ref(clazz.getSimpleName());
        return result;
    }

}
