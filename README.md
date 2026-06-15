# Engineering Brain MVP

Flow:

Upload docs / repo
    ->
Chunk
    ->
Embeddings
    ->
Qdrant
    ->
Question
    ->
Retrieve chunks
    ->
LLM answer

Endpoints:

POST /api/upload
POST /api/chat