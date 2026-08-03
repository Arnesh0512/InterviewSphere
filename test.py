import psycopg2

DB_HOST = "aws-0-ap-southeast-1.pooler.supabase.com"
DB_PORT = "6543"
DB_NAME = "postgres"
DB_USER = "postgres.thpcbzsmbccfrcqavnbd"
DB_PASS = "gr8india@Arnesh512"

try:
    print("Attempting to connect via Supabase Pooler...")
    connection = psycopg2.connect(
        host=DB_HOST,
        database=DB_NAME,
        user=DB_USER,
        password=DB_PASS,
        port=DB_PORT,
        sslmode="require",
        connect_timeout=10
    )
    
    cursor = connection.cursor()
    cursor.execute("SELECT version();")
    db_version = cursor.fetchone()
    print("\n✅ Successfully connected to Supabase via Pooler!")
    print(f"Database Version: {db_version[0]}")
    
    cursor.close()
    connection.close()
    
except Exception as error:
    print("\n❌ Connection failed!")
    print(error)