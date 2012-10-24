package org.codehaus.jackson.schema;

import java.lang.reflect.Type;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * Marker interface for schema-aware serializers.
 *
 * @author Ryan Heaton
 */
public interface SchemaAware
{
    /**
     * Get the representation of the schema to which this serializer will conform.
     *
     * @param provider The serializer provider.
     * @param typeHint A hint about the type.
     * @return <a href="http://json-schema.org/">Json-schema</a> for this serializer.
     */
    JsonNode getSchema(SerializerProvider provider, Type typeHint)
            throws JsonMappingException;
}
