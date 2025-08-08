package org.example.bff.utils;

import org.springframework.stereotype.Component;

@Component
public class LLMInstructionsGenerator {
    private static final String NOT_FINE_TUNED_INSTRUCTION = """
            You will receive an input in the exact form:
            "context=... prompt='...' schema=..."
            
            Goal: produce a single-line response that is either an `OK` with a ready-to-send GraphQL operation or an `ERROR` with a concise explanation. The final assistant output must be exactly one textual value in this shape (no surrounding quotes, no code fences, no extra commentary):
            
            <STATUS>:<MESSAGE>
            
            - STATUS must be exactly `OK` or `ERROR` (uppercase).
            - If STATUS is `OK`, MESSAGE must contain **only** the GraphQL operation text (a valid `query` or `mutation`) and nothing else.
            - If STATUS is `ERROR`, MESSAGE must be a short, concrete explanation of what information was missing or ambiguous.
            
            INPUT STRUCTURE
            - `context` (first field) contains metadata, runtime values, or defaults the caller may supply (for example latitude, longitude, userId, preferred page size, default depth, or other domain-specific parameters). `context` can be any text format but will usually contain JSON-like key/value pairs or plain keys — parse it to extract values whose names match schema arguments/inputs.
            - `prompt` (second field) describes the user intent and may include explicit values to use in the operation.
            - `schema` (third field) contains the full GraphQL schema text (the contents of your .graphql file) and is authoritative: always consult it.
            
            PRECENDENCE & MERGING RULES FOR VALUES (new)
            - **Precedence:** If a value is supplied both in `prompt` and in `context`, **prompt** wins; use the prompt value.
            - **Filling required inputs:** For any required (non-null) argument or input field found in the schema, try to find a value in this order:
             1. explicit value in the `prompt` (e.g., `id = "123"`);
             2. matching key/value in `context` (e.g., `latitude`, `longitude`, `userId`);
             3. if neither provides it, return `ERROR:` listing the missing required argument(s).
            - **Nested inputs:** If the schema expects an input object, `context` may provide nested maps; merge prompt-specified nested fields over context-specified nested fields.
            - **Variables vs literals:** Inline values as literals unless the prompt explicitly requests variable usage. Values taken from `context` are also inlined unless variables are requested.
            - **Type coercion:** Convert context/prompt values to the correct GraphQL literal form:
             - strings → `"..."` (escape quotes),
             - numbers → unquoted numeric literals,
             - booleans → `true` / `false`,
             - enums → unquoted IDENTIFIERS (e.g., `PARKING`),
             - null / omitted → omitted (or ERROR if required).
            - **Do not invent** any values; only use values present in prompt or context.
            
            STRICT RULES (must follow exactly)
            1. Schema-first: Always parse and respect the schema. Any type/field/argument/input referenced must exist in that schema.
            2. Operation choice:
              - If the prompt clearly asks to create/update/delete (words such as create, update, delete, patch, mutate), produce a `mutation`.
              - Otherwise produce a `query`.
            3. Fields to return:
              - If the prompt specifies fields, return exactly those fields (and required nested subfields).
              - If the prompt requests "all data", "everything", or omits fields, return all scalar fields and expand object fields up to the Default Depth (see Defaults).
            4. Arguments and inputs:
              - Inline concrete values from prompt or context as described above.
              - If required inputs are missing from both prompt and context, return an ERROR listing the missing names.
              - Use GraphQL variables only if the prompt explicitly requests them.
            5. Lists and pagination:
              - When selecting list fields, include a reasonable page size argument if the schema supports it (e.g., `first: 100` or `limit: 100`) unless the prompt explicitly requests all items or the context overrides the page size.
            6. Validation and errors:
              - If any referenced name (type, field, argument, input) is not present in the schema, return `ERROR:<missing name(s)>`.
              - If the prompt is ambiguous (e.g., both `Query.user(id: ID!)` and `Query.users(filter: UserFilter)` exist and the prompt doesn't specify), return `ERROR:` explaining the ambiguity and what is needed (id or filter).
            7. Leaf-type rule:
              - If a field's type is an `ENUM` or `SCALAR` (including custom scalars), it is a leaf: **do not** produce a selection set or inline fragment for it.
              - If a planned selection includes subselections for a leaf type, return:
                `ERROR:InvalidSelectionOnLeafType — field '<fieldName>' is of type '<TypeName>' which is a <enum|scalar> and cannot have subselections.`
              - Use inline fragments only for interfaces/unions/objects.
            8. Formatting and syntax:
              - MESSAGE must be valid GraphQL syntax. You may include newlines and indentation inside MESSAGE.
              - Do not include comments, metadata, or explanation in the output.
            9. Output convention:
              - Always produce a *single* line that begins with `OK:` or `ERROR:`. After `OK:` the GraphQL operation may contain newlines.
            10. Defaults:
              - Default nested expansion depth: 2 (root -> object -> its scalar fields). If the prompt or context includes `depth=N`, use that value.
              - Default list page size: `first: 100` or schema-equivalent (page, limit) if present in the schema. A `pageSize` in `context` should override the default.
            11. Error messages:
              - Be specific and concise. Example: `ERROR:Missing required input fields for mutation CreateUser — specify name, email`.
            
            EXAMPLES (compact, exact input -> exact output)
            
            1) Required args supplied via `context` (preferred behavior)
            Schema:
            type Query { searchPOIs(request: SearchPOIsRequest!): [POI!]! }
            input SearchPOIsRequest { latitude: Float! longitude: Float! services: [POIService!] isOpenNow: Boolean }
            enum POIService { PARKING RESTAURANT }
            type POI { id: ID! name: String! services: [POIService!]! }
            
            Input:
            context={"latitude":30.3321838,"longitude":-81.655651} prompt='parques de estacionamento abertos com restaurante' schema={above}
            
            Output:
            OK:query { searchPOIs(request:{ latitude:30.3321838 longitude:-81.655651 services:[PARKING, RESTAURANT] isOpenNow:true }) { id name services } }
            
            2) Missing required arg (error)
            If neither prompt nor context provides required `latitude`:
            Output:
            ERROR:Missing required argument(s) for searchPOIs.request: latitude
            
            IMPLEMENTATION NOTES FOR THE LLM
            - Always consult the provided `schema` text as authoritative.
            - Use values from `prompt` first, then fall back to `context` for missing required or optional inputs.
            - Do not fabricate fields, types, or input shapes.
            - Keep queries minimal but complete.
            - If you cannot produce a valid GraphQL operation from the supplied context and prompt, return `ERROR:` followed by the exact missing or ambiguous items.
            
            End of instruction.
            """;

    public String generateNotFineTunedInstruction(final String context, final String prompt, final String schema) {
        return (NOT_FINE_TUNED_INSTRUCTION +
                String.format("context=%s prompt='%s' schema=%s", context, prompt, schema))
                .replace("\n", "\\n")
                .replace("\"", "\\\"");
    }
}
