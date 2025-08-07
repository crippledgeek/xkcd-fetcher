package se.disabledsecurity.xkcd.fetcher.external.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class XkcdTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldDeserializeJsonExample() throws Exception {
        // Given
        String jsonExample = "{\"month\": \"7\", \"num\": 614, \"link\": \"\", \"year\": \"2009\", \"news\": \"\", \"safe_title\": \"Woodpecker\", \"transcript\": \"[[A man with a beret and a woman are standing on a boardwalk, leaning on a handrail.]]\\nMan: A woodpecker!\\n<<Pop pop pop>>\\nWoman: Yup.\\n\\n[[The woodpecker is banging its head against a tree.]]\\nWoman: He hatched about this time last year.\\n<<Pop pop pop pop>>\\n\\n[[The woman walks away.  The man is still standing at the handrail.]]\\n\\nMan: ... woodpecker?\\nMan: It's your birthday!\\n\\nMan: Did you know?\\n\\nMan: Did... did nobody tell you?\\n\\n[[The man stands, looking.]]\\n\\n[[The man walks away.]]\\n\\n[[There is a tree.]]\\n\\n[[The man approaches the tree with a present in a box, tied up with ribbon.]]\\n\\n[[The man sets the present down at the base of the tree and looks up.]]\\n\\n[[The man walks away.]]\\n\\n[[The present is sitting at the bottom of the tree.]]\\n\\n[[The woodpecker looks down at the present.]]\\n\\n[[The woodpecker sits on the present.]]\\n\\n[[The woodpecker pulls on the ribbon tying the present closed.]]\\n\\n((full width panel))\\n[[The woodpecker is flying, with an electric drill dangling from its feet, held by the cord.]]\\n\\n{{Title text: If you don't have an extension cord I can get that too.  Because we're friends!  Right?}}\", \"alt\": \"If you don't have an extension cord I can get that too.  Because we're friends!  Right?\", \"img\": \"https://imgs.xkcd.com/comics/woodpecker.png\", \"title\": \"Woodpecker\", \"day\": \"24\"}";

        // When
        Xkcd xkcd = objectMapper.readValue(jsonExample, Xkcd.class);

        // Then
        assertNotNull(xkcd);
        assertEquals("7", xkcd.month());
        assertEquals(614, xkcd.num());
        assertEquals("", xkcd.link());
        assertEquals("2009", xkcd.year());
        assertEquals("", xkcd.news());
        assertEquals("Woodpecker", xkcd.safe_title());
        assertTrue(xkcd.transcript().startsWith("[[A man with a beret"));
        assertEquals("If you don't have an extension cord I can get that too.  Because we're friends!  Right?", xkcd.alt());
        assertEquals("https://imgs.xkcd.com/comics/woodpecker.png", xkcd.img());
        assertEquals("Woodpecker", xkcd.title());
        assertEquals("24", xkcd.day());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        Xkcd xkcd = new Xkcd(
            "7",
            "",
            "2009",
            "",
            "Woodpecker",
            "[[A man with a beret and a woman are standing on a boardwalk, leaning on a handrail.]]",
            "If you don't have an extension cord I can get that too.  Because we're friends!  Right?",
            "Woodpecker",
            "24",
            614,
            "https://imgs.xkcd.com/comics/woodpecker.png"
        );

        // When
        String json = objectMapper.writeValueAsString(xkcd);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"month\":\"7\""));
        assertTrue(json.contains("\"num\":614"));
        assertTrue(json.contains("\"year\":\"2009\""));
        assertTrue(json.contains("\"safe_title\":\"Woodpecker\""));
        assertTrue(json.contains("\"title\":\"Woodpecker\""));
        assertTrue(json.contains("\"day\":\"24\""));
        assertTrue(json.contains("\"img\":\"https://imgs.xkcd.com/comics/woodpecker.png\""));
    }
}