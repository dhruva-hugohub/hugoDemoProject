package com.hugo.demo.protobuf;

import com.google.protobuf.util.JsonFormat;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;

public class ProtoMessageConverter {
    private final JsonFormat.TypeRegistry typeRegistry;
    private final JsonFormat.Parser parser;
    private final JsonFormat.Printer printer;

    public ProtoMessageConverter(JsonFormat.TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;

        this.parser = JsonFormat.parser()
            .usingTypeRegistry(typeRegistry)
            .ignoringUnknownFields();

        this.printer = JsonFormat.printer()
            .usingTypeRegistry(typeRegistry)
            .preservingProtoFieldNames()
            .includingDefaultValueFields()
            .omittingInsignificantWhitespace();
    }

    public JsonFormat.Parser getParser() {
        return this.parser;
    }

    public JsonFormat.Printer getPrinter() {
        return this.printer;
    }

    public ProtobufJsonFormatHttpMessageConverter getHttpConverter(boolean usingProtoFieldNames) {
        JsonFormat.Printer httpConverterPrinter = JsonFormat.printer()
            .usingTypeRegistry(this.typeRegistry)
            .includingDefaultValueFields()
            .omittingInsignificantWhitespace();

        if (usingProtoFieldNames) {
            httpConverterPrinter = httpConverterPrinter.preservingProtoFieldNames();
        }

        return new ProtobufJsonFormatHttpMessageConverter(this.parser, httpConverterPrinter);
    }

    public JsonFormat.TypeRegistry getTypeRegistry() {
        return this.typeRegistry;
    }
}
