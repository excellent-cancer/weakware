package collaborative.engine.content.yaml;

import collaborative.engine.content.common.LineColumn;
import collaborative.engine.content.common.Paragraph;
import collaborative.engine.content.core.Token;
import pact.annotation.DataStruct;

/**
 * @author XyParaCrim
 */
@DataStruct
public class YamlToken extends Token {

    public static final YamlToken DUMMY = new YamlToken(Paragraph.identical(LineColumn.ORIGIN), YamlTokenKind.DUMMY);

    private YamlToken(Paragraph paragraph, YamlTokenKind kind) {
        super(paragraph, kind);
    }

    private YamlToken(Paragraph paragraph, YamlTokenKind kind, String content) {
        super(paragraph, kind, content);
    }

    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public static YamlToken EOF(LineColumn lineColumn) {
        return new YamlToken(Paragraph.identical(lineColumn), YamlTokenKind.EOF);
    }

    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public static YamlToken EOF(Paragraph paragraph) {
        return new YamlToken(paragraph, YamlTokenKind.EOF);
    }

    public static YamlToken comment(Paragraph paragraph) {
        return new YamlToken(paragraph, YamlTokenKind.COMMENT);
    }

    public static YamlToken literal(Paragraph paragraph, String content) {
        return new YamlToken(paragraph, YamlTokenKind.LITERAL, content);
    }

    public static YamlToken error(Paragraph paragraph) {
        return new YamlToken(paragraph, YamlTokenKind.ERROR);
    }

    public static YamlToken split(Paragraph paragraph) {
        return new YamlToken(paragraph, YamlTokenKind.SPLIT);
    }

    public static YamlToken item(Paragraph paragraph) {
        return new YamlToken(paragraph, YamlTokenKind.ITEM);
    }

    /**
     * This enum defines partial token used by the Yaml scanner. Similar
     * to Token#TokenKind from sun-tools.
     */
    @DataStruct
    public enum YamlTokenKind implements TokenKind {
        /**
         * virtual token or origin line-column
         */
        DUMMY(),
        /**
         * end of yaml content
         */
        EOF(),
        /**
         * where the lexical error first appeared in the text
         */
        ERROR(),
        /**
         * line comment
         */
        COMMENT(),
        /**
         * consecutive reasonable strings in text
         */
        LITERAL(),
        /**
         * key and value delimiters
         */
        SPLIT(":"),
        /**
         * the beginning of an array item
         */
        ITEM("-");

        public final String name;

        YamlTokenKind() {
            this(null);
        }

        YamlTokenKind(String name) {
            this.name = name;
        }
    }
}
