import os
from pymongo import MongoClient
from pymongo.server_api import ServerApi
import psycopg2
from psycopg2.extras import execute_values

# --- Configuration from your project ---
MONGO_URI = "mongodb+srv://arnesh052006:IEEEinterview@cluster.m7y1vxr.mongodb.net/?appName=Cluster"
DB_NAME = "interview"
COLLECTION_NAME = "leetcode"

# PostgreSQL connection details parsed from your application.properties pooler string
# Host: aws-0-ap-southeast-1.pooler.supabase.com, Port: 6543, DB: postgres
PG_HOST = "aws-0-ap-southeast-1.pooler.supabase.com"
PG_PORT = "6543"
PG_DATABASE = "postgres"
PG_USER = "postgres.thpcbzsmbccfrcqavnbd"
PG_PASSWORD = "gr8india@Arnesh512"

def migrate_data():
    print("Connecting to MongoDB...")
    mongo_client = MongoClient(MONGO_URI, server_api=ServerApi('1'))
    mongo_db = mongo_client[DB_NAME]
    leetcode_collection = mongo_db[COLLECTION_NAME]

    # Fetch all documents from MongoDB collection
    documents = list(leetcode_collection.find({}))
    print(f"Found {len(documents)} documents in MongoDB collection '{COLLECTION_NAME}'.")

    if not documents:
        print("No documents to migrate.")
        return

    print("Connecting to Supabase (PostgreSQL)...")
    pg_conn = psycopg2.connect(
        host=PG_HOST,
        port=PG_PORT,
        database=PG_DATABASE,
        user=PG_USER,
        password=PG_PASSWORD,
        sslmode="require"
    )
    pg_cursor = pg_conn.cursor()

    insert_query = """
        INSERT INTO leetcode_questions (question_id, task_name, difficulty, problem_description, tags, companies)
        VALUES %s
        ON CONFLICT (question_id) DO UPDATE SET
            task_name = EXCLUDED.task_name,
            difficulty = EXCLUDED.difficulty,
            problem_description = EXCLUDED.problem_description,
            tags = EXCLUDED.tags,
            companies = EXCLUDED.companies;
    """

    rows_to_insert = []
    for doc in documents:
        question_id = doc.get("question_id")
        task_name = doc.get("task_name")
        difficulty = doc.get("difficulty")
        problem_description = doc.get("problem_description")
        
        # Ensure lists are formatted correctly for PostgreSQL text[] arrays
        tags = doc.get("tags", [])
        companies = doc.get("companies", [])

        if question_id is not None:
            rows_to_insert.append((
                question_id,
                task_name,
                difficulty,
                problem_description,
                tags,
                companies
            ))

    try:
        # Bulk insert/upsert using psycopg2 execute_values
        execute_values(pg_cursor, insert_query, rows_to_insert)
        pg_conn.commit()
        print(f"Successfully migrated {len(rows_to_insert)} records to 'leetcode_questions' table in Supabase!")
    except Exception as e:
        pg_conn.rollback()
        print(f"❌ Error during insertion into Supabase: {e}")
    finally:
        pg_cursor.close()
        pg_conn.close()
        mongo_client.close()

if __name__ == "__main__":
    migrate_data()