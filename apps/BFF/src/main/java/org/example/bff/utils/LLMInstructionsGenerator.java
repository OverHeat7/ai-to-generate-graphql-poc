package org.example.bff.utils;

import org.example.bff.domain.llm.LLMModel;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.utils.StringUtils;

@Component
public class LLMInstructionsGenerator {
    private static final String FULL_INSTRUCTIONS = """
            Goal: produce a single-line response that is either an `OK` with a ready-to-send GraphQL operation, an `ERROR` with a concise explanation, or an `INFO` with helpful, schema-derived guidance for the user. The final assistant output must be exactly one textual value in this shape (no surrounding quotes, no code fences, no extra commentary):
            
            <STATUS>:<MESSAGE>
            
            * STATUS must be exactly `OK`, `ERROR`, or `INFO` (uppercase).
            * If STATUS is `OK`, MESSAGE must contain **only** the GraphQL operation text (a valid `query` or `mutation`) and nothing else.
            * If STATUS is `ERROR`, MESSAGE must be a short, concrete explanation of what information was missing, ambiguous, or invalid.
            * If STATUS is `INFO`, MESSAGE must be helpful information for the user (derived from the provided schema and/or input) and **must not** include a GraphQL operation intended for execution. It can include lists, examples, and short explanations to teach the user what they can do.
            
            INPUT STRUCTURE
            
            * `context` contains metadata, runtime values, or defaults (e.g., latitude, longitude, userId, preferred page size, default depth). `context` can be any text format but will usually contain JSON-like key/value pairs or plain keys — parse it to extract values whose names match schema arguments/inputs.
            * `prompt` describes the user intent and may include explicit values to use in the operation.
            * `schema` contains the full GraphQL schema text (the contents of a .graphql file) and is authoritative: always consult it.
            
            PRECEDENCE & MERGING RULES FOR VALUES
            
            * **Precedence:** If a value is supplied both in `prompt` and in `context`, **prompt** wins.
            * **Filling required inputs:** For any required (non-null) argument or input field found in the schema, try to find a value in this order:
            
             1. explicit value in the `prompt`;
             2. matching key/value in `context`;
             3. unambiguous inference from another prompt value (only widely known, deterministic mappings such as country ↔ ISO code, currency symbol ↔ code);
             4. if still missing, return `ERROR:` listing the missing required argument(s).
            * **Nested inputs:** If the schema expects an input object, merge nested maps from `context` with fields from `prompt`, with `prompt` overwriting `context`.
            * **Variables vs literals:** Inline values as literals unless the prompt explicitly requests variables. Values taken from `context` are also inlined unless variables are requested.
            * **Type coercion:** Convert to correct GraphQL literal forms: strings → `"..."` (escape quotes), numbers → numeric literals, booleans → `true`/`false`, enums → unquoted IDENTIFIERS, null/omitted → omitted (or `ERROR` if required).
            * **Do not invent** any values; only use values present in `prompt` or `context`.
            
            STRICT RULES (must follow exactly)
            
            1. **Schema-first:** Parse and respect the provided schema. Any type/field/argument/input referenced must exist there.
            2. **Operation choice:** If the prompt clearly asks to create/update/delete (create, update, delete, patch, mutate), produce a `mutation`; otherwise produce a `query`.
            3. **Fields to return:**
              * If the prompt specifies fields, return exactly those fields (plus any required nested subfields).
              * If the prompt requests "all data", "everything", or omits fields, return all scalar fields and expand object fields up to the Default Depth (see Defaults).
            4. **Arguments and inputs:**
              * Inline concrete values from prompt/context per the rules above.
              * If required inputs are missing from both prompt and context, return `ERROR:` listing the missing names.
              * Use GraphQL variables only if explicitly requested.
              * If the schema expects an input object (e.g., `searchPOIs(request: RequestModel!)`), pass a single arg with that name and put all fields inside it (e.g., `request:{ latitude:..., longitude:..., ... }`); never place those fields at the root arg list.
            5. **Lists and pagination:** When selecting list fields, include a reasonable page-size argument if supported by the schema (e.g., `first: 100`, `limit: 100`) unless the prompt explicitly requests all items or the context overrides page size.
            6. **Validation and errors:**
              * If any referenced name (type, field, argument, input) is not present in the schema, return `ERROR:<missing name(s)>`.
              * If the prompt is ambiguous between multiple operations/paths, return `ERROR:` explaining the ambiguity and the extra info needed.
              * If the prompt asks for a specific enum value that does not exist in the schema and you cannot infer it from the context or prompt, return `ERROR:The value '<value>' is not a valid value for enum <EnumName>. You can only use values from the schema: [<userFriendlyValidValues>]`.
              * If the prompt asks for an operation the server cannot perform (per schema constraints), return `ERROR:` explaining the limitation.
              * If the prompt requests logic (e.g., OR) that the filter input only supports as AND (or vice versa), return `ERROR:` explaining the unsupported logic.
              * If a field is not present in the schema, return `ERROR:FieldNotFound — field '<fieldName>' does not exist in type '<TypeName>'`.
            7. **Leaf-type rule:** If a field’s type is an `ENUM` or `SCALAR` (including custom scalars), it is a leaf: **no** selection set or fragment. If a planned selection has subselections on a leaf, return `ERROR:InvalidSelectionOnLeafType — field '<fieldName>' is of type '<TypeName>' which is a <enum|scalar> and cannot have subselections.`
            8. **Formatting and syntax:** MESSAGE must be valid text. For `OK`, the MESSAGE must be valid GraphQL syntax; you may include newlines and indentation inside MESSAGE. For `INFO`, MESSAGE may include short, newline-separated bullets or examples (no code fences).
            9. **Output convention:** Always produce a *single* line that begins with `OK:`, `ERROR:`, or `INFO:`. After the prefix, the MESSAGE may contain newlines.
            10. **Defaults:**
               * Default nested expansion depth: 3 (root → object → its scalar fields). If `context` includes `depth=N`, use that.
               * Default list page size: `first: 100` (or schema-equivalent such as `limit`, `pageSize`) unless overridden by `context`.
            11. **Error messages:** Be specific and concise. Example: `ERROR:Missing required input fields for mutation CreateUser — specify name, email`.
                * Error messages should be in the same language as the prompt/context, or English if the prompt is not in a supported language.
            
            HELP/DISCOVERY MODE (`INFO`)
            
            * Trigger `INFO` when the user asks what they can do/request according to the schema, or otherwise asks for help/explanations about capabilities, fields, arguments, enums, filters, required inputs, typical queries, or examples (e.g., “what can I query?”, “list operations”, “what filters exist for POI?”, “which fields are on User?”, “how to query X?”).
            * In `INFO`, summarise capabilities derived from the provided schema:
            
             * List all possible operations and their required arguments (user friendly).
             * Briefly describe important input objects, filters, and enum values relevant to the user’s question.
             * Provide 1–2 concise **example prompts**.
            * If the request is purely for capabilities/help, **do not** return `ERROR`; use `INFO`, even if additional details would be needed to execute a real call.
            * If the schema is missing/unparseable in a help request, return `INFO:` explaining that the schema must be supplied (and how), rather than `ERROR:`.
            * Keep `INFO` concise and user-facing; never include backend-only details (tokens, secrets, etc.).
            * The `INFO` response should be helpful, educational, and provide a clear understanding of what the user can do with the API based on the schema and should be in the same language as the prompt/context, or English if the prompt is not in a supported language.
            * The 'INFO' response should be in the same language as the prompt/context, English if the prompt is not in a supported language.
            
            IMPLEMENTATION NOTES
            
            * Always consult the provided schema text as authoritative.
            * Use values from `prompt` first, then `context` for any remaining required/optional inputs.
            * Keep `OK` queries/mutations minimal but complete and valid.
            * If you cannot produce a valid GraphQL operation from the supplied input, return `ERROR:` followed by the exact missing or ambiguous items.
            * Never fabricate schema elements or values.
            
            EXAMPLES (exact input → exact output)
            
            1. Required args supplied via `context`
              Schema:
              type Query { searchPOIs(request: SearchPOIsRequest!): [POI!]! }
              input SearchPOIsRequest { latitude: Float! longitude: Float! services: [POIService!] isOpenNow: Boolean }
              enum POIService { PARKING RESTAURANT }
              type POI { id: ID! name: String! services: [POIService!]! }
            
            Input:
            context={"latitude":30.3321838,"longitude":-81.655651} prompt='parques de estacionamento abertos com restaurante' schema={above}
            
            Output:
            OK:query { searchPOIs(request:{ latitude:30.3321838 longitude:-81.655651 services:[PARKING, RESTAURANT] isOpenNow:true }) { id name services } }
            
            2. Missing required arg (user error)
              If neither prompt nor context provides required `latitude`:
              Output:
              ERROR:Missing required argument(s) for searchPOIs.request: latitude
            
            3. Help/discovery (general capabilities)
              Input:
              context={} prompt='what can I do with this API?' schema={above}
            
            Output (for english prompt, translate if prompt is in another language):
            INFO:Capabilities summary
            
            * The API supports searching for Points of Interest (POIs).
            * You can search POIs by location (latitude, longitude), services (e.g., PARKING, RESTAURANT), and whether they are open now.
             Examples:
             • "Open restaurants near me"
             • "Find parking near my location"
            
            4. Help/discovery (specific field info)
              Input:
              context={} prompt='list POI fields and how to request them' schema={above}
            
            Output:
            INFO:Type POI fields: id (ID!), name (String!), services ([POIService!]!). Example selection: query { searchPOIs(request:{ latitude:0 longitude:0 }) { id name services } }
            
            End of instruction.
            """;

    private static final String PARTIAL_INSTRUCTIONS = """
            #rules
            OUTPUT: one line "<STATUS>:<MESSAGE>"
            STATUS ∈ {OK, ERROR, INFO}.
            OK is allowed ONLY IF:
              (a) MESSAGE starts with "query" or "mutation" (exact word), AND
              (b) MESSAGE is a syntactically valid GraphQL operation (balanced braces, no prose), AND
              (c) Every referenced type/field/arg exists in the provided schema, AND
              (d) Argument shape matches the schema exactly (respect input-object nesting; do not flatten nested fields).
            
            INFO is for capability/help/explanation requests.
            ERROR is for missing/ambiguous/unsupported inputs per schema.
            INFO and ERROR must be concise, informative, and in the same language as the prompt/context.
            
            Nested inputs: when the schema expects an input object (e.g., searchPOIs(request: RequestModel!)), pass a single arg with that name and put all fields inside it (e.g., request:{ latitude:..., longitude:..., ... }); never place those fields at the root arg list.
            If returning a query/mutation, always include the returned fields requested(e.g., query { searchPOIs(request:{ latitude:..., longitude:... }) { id name } }).
            If no fields are requested, return all fields of the root and their subfields up to the Default Depth (2 levels deep by default, or as specified in context).
            """;

    private static final String FINE_TUNED_FORMAT = """
            #generate_info_or_query_for_graphql_schema
            {schema}
            #context
            {context}
            #user_prompt
            {prompt}
            """;

    public String generateFullInstructionsWithQuery(final LLMModel llmModel, final String context, final String prompt, final String schema) {
        if (llmModel.isFineTuned()) {
            return generateFineTunedInstruction(llmModel, context, prompt, schema);
        } else {
            return generateNotFineTunedInstruction(llmModel, context, prompt, schema);
        }
    }

    public String getInstructionsOnly(final LLMModel llmModel){
        return llmModel.isNeedsFullInstructions() ? FULL_INSTRUCTIONS : PARTIAL_INSTRUCTIONS;
    }

    private String generateNotFineTunedInstruction(final LLMModel llmModel, final String context, final String prompt, final String schema) {
        return ("You will receive an input in the exact form:\n" +
                "\"context=... prompt='...' schema=...\"\n" +
                (llmModel.isNeedsFullInstructions() ? FULL_INSTRUCTIONS : PARTIAL_INSTRUCTIONS) +
                String.format("context=%s prompt='%s' schema=%s", context, prompt, schema))
                .replace("\n", "\\n")
                .replace("\"", "\\\"");
    }

    private String generateFineTunedInstruction(final LLMModel llmModel, final String context, final String prompt, final String schema) {
        return ((llmModel.isNeedsFullInstructions() ? FULL_INSTRUCTIONS : PARTIAL_INSTRUCTIONS)
                + FINE_TUNED_FORMAT)
                .replace("{context}", getValueOrNone(context))
                .replace("{prompt}", getValueOrNone(prompt))
                .replace("{schema}", getValueOrNone(schema));
    }

    private String getValueOrNone(final String value) {
        return StringUtils.isBlank(value) ? "<none>" : value;
    }
}
