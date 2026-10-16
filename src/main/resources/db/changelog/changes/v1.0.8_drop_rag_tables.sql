--liquibase formatted sql

--changeset fit-team:001-drop-rag-tables
--comment Drop RAG and vector store tables (rag_documents, vector_store)

DROP TABLE IF EXISTS public.rag_documents CASCADE;
DROP TABLE IF EXISTS public.vector_store CASCADE;
