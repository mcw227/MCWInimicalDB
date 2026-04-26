import pandas as pd
from sqlalchemy import create_engine
import oracledb
import numpy as np
from faker import Faker
from faker_food import FoodProvider

fake = Faker()
fake.add_provider(FoodProvider)

# Credentials
USER = 'mcw227'
PASSWORD= 'P800081240'
HOST = 'rocordb01.cse.lehigh.edu'
PORT = '1522'
SERVICE_NAME = 'cse241pdb'

connection_url = f"oracle+oracledb://{USER}:{PASSWORD}@{HOST}:{PORT}/?service_name={SERVICE_NAME}"

def csv_to_oracle(file_path, table_name):
    try:

        engine = create_engine(connection_url)

        with engine.begin() as connection: 
            df = pd.read_csv(file_path)
            df.to_sql(table_name, con=connection, if_exists='delete_rows', index=False, chunksize=1000)

            print(f"Success! Data uploaded to Oracle table: {table_name}")
    
        print("Connection closed automatically!")

    except Exception as e:
        print(f"Oracle Connection Error: {e}")

# TAKES THE CSV OF CUSTOMERS AND ASSIGNS THEM ROLES
def gen_customers():
    df = pd.read_csv('Mock Data/CUSTOMER_DATA.csv')
    df = df.drop(columns=['pass'])
    df["membership"] = np.random.choice([1,0], size=len(df), p=[0.6,0.4])
    df["points"] = 0
    df["active"] = 1

    mask = df["membership"] == 1
    df.loc[mask, "points"] = np.random.randint(0,10000, size=mask.sum())


    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('customers', con = connection, if_exists='append', index=False, chunksize=1000)
            print(f"Success! Data uploaded to Oracle table: phone_numbers")
        print("Connection closed automatically!")
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")


def gen_employees():
    df = pd.read_csv('Mock Data/EMPLOYEE_DATA.csv')
    df['role'] = np.random.choice([1,2], size=len(df), p=[0.7,0.3]) # weighted so we dont end up with a bunch of general managers...
    df['location_id'] = np.random.randint(1,101, size=len(df))

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('employees', con=connection, if_exists='delete_rows', index=False, chunksize=1000)
            print(f"Success! Data uploaded to Oracle table: employees")

    except Exception as e:
        print(f"Oracle Connection Error: {e}")
    
# TAKES THE CSV OF RANDOM PHONE NUMBERS AND ASSIGNS IT TO RANDOM CUSTOMERS
def gen_phones():
    df = pd.read_csv('Mock Data/PHONE_DATA.csv')

    df['customer_id'] = np.random.randint(2, 101, size=len(df))

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('phone_numbers', con = connection, if_exists='append', index=False, chunksize=1000)
            print(f"Success! Data uploaded to Oracle table: phone_numbers")
        print("Connection closed automatically!")
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")

# TAKES THE CSV OF RANDOM CARDS AND ASSIGNS IT TO RANDOM CUSTOMERS
def gen_cards():
    df = pd.read_csv('Mock Data/CARD_DATA.csv')

    df['customer_id'] = np.random.randint(2, 101, size=len(df))
    df['active'] = 1

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('cards', con = connection, if_exists='append', index=False, chunksize=1000)
            print(f"Success! Data uploaded to Oracle table: cards")
        print("Connection closed automatically!")
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")

# Generates rows for the basic menus
def gen_menus():

    df = pd.DataFrame(columns=["name"])
    df["name"] = ["Lunch","Dinner", "Dessert"]

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('menus', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: menus")
        print("Connection closed automatically!")
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")

# Generates items
def gen_items():

    df = pd.DataFrame(columns=["name","price"])
    df["name"] = [fake.ingredient() for _ in range(45)]

    raw_data = np.random.uniform(0.00, 10.00, size=45)

    formatted_numbers = np.round(raw_data, 2)
    df["price"] = formatted_numbers

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('items', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: items")
        print("Connection closed automatically!")
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")

def gen_signatures():
    df = pd.DataFrame(columns=["name","price"])
    df["name"] = [fake.dish() for _ in range(45)]

    raw_data = np.random.uniform(5.00, 50.00, size=45)

    formatted_numbers = np.round(raw_data, 2)
    df["price"] = formatted_numbers

    num_sigs = pd.DataFrame(columns=["id"])
    num_sigs['id'] = np.arange(46, 91)

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('items', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: items")

        with engine.begin() as connection:
            num_sigs.to_sql('signature_items', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: signature_items")

        print("Connection closed automatically!")
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")

# generates the name of a customer creation
def gen_cc_name():
    return f"{fake.first_name()}'s {fake.word(part_of_speech='adjective')} {fake.dish()}"


# generates customer creations
def gen_cc():
    df = pd.DataFrame(columns=["name","price"])
    df["name"] = [gen_cc_name() for _ in range(10)]

    df["price"] = 0

    num_sigs = pd.DataFrame(columns=["id"])
    num_sigs['id'] = np.arange(91, 101)

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('items', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: items")

        with engine.begin() as connection:
            num_sigs.to_sql('customer_creations', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: customer_creations")

        print("Connection closed automatically!")
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")

def gen_unique_pairs(x,y,n):
    col1 = np.random.randint(1, x, size=n)
    col2 = np.random.randint(1, y, size=n)
    
    # Check where they are equal
    equal_mask = (col1 == col2)
    
    # While there are duplicates within a pair, re-roll those specific rows
    while equal_mask.any():
        col2[equal_mask] = np.random.randint(1, 101, size=equal_mask.sum())
        equal_mask = (col1 == col2)
    
    return np.column_stack((col1, col2))

def gen_menu_items():
    df = pd.DataFrame(gen_unique_pairs(4,101,10), columns=["menu_id", "item_id"])

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('menu_items', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: menu_items")

        print("Connection closed automatically!")
        return True
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")
        return False

# generates recipes for customer creations
def gen_recipes():
    df = pd.DataFrame(columns=["recipe_id","ingredient_id","quantity"])
    seq = np.arange(91,101)

    # Repeat the sequence to fit the length of the DataFrame
    df["recipe_id"] = np.tile(seq, 37 // len(seq) + 1)[:37]

    df["ingredient_id"] = np.random.randint(1,91,37)
    df["quantity"] = np.random.randint(1,6,37)

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('recipes', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: recipes")

        print("Connection closed automatically!")
        return True
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")
        return False

# generates customer order numbers
def gen_orders():
    df = pd.DataFrame(columns=["location_id","customer_id","payment_id"])
    df["location_id"] = np.random.randint(1,101,15)
    df["customer_id"] = np.random.randint(1,101,15)
    df["payment_id"] = np.random.randint(1,101,15)
    df["price"] = 0
    df["status"] = 4
 
    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('orders', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: orders")

        print("Connection closed automatically!")
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")   

# generates items in customer orders
def gen_order_items():
    df = pd.DataFrame(gen_unique_pairs(16, 91, 27), columns=["order_id","item_id"])

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('order_items', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: order_items")

        print("Connection closed automatically!")
        return True
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")
        return False

def gen_price_change():
    df = pd.DataFrame(gen_unique_pairs(101, 101, 50), columns=["item_id","location_id"])
    raw_data = np.random.uniform(0.00, 10.00, size=50)

    formatted_numbers = np.round(raw_data, 2)
    df["price"] = formatted_numbers

    try:
        engine = create_engine(connection_url)

        with engine.begin() as connection:
            df.to_sql('price_change', con = connection, if_exists='append', index=False)
            print(f"Success! Data uploaded to Oracle table: price_change")

        print("Connection closed automatically!")
    
    except Exception as e:
        print(f"Oracle Connection Error: {e}")


# RUNS DB POPULATION! REQUIRES EMPTY TABLES.
def populate_db():
    csv_to_oracle('./Mock Data/LOCATION_DATA.csv', 'locations')
    gen_employees()
    gen_customers()
    gen_phones()
    gen_cards()
    gen_menus()
    gen_items()
    gen_signatures()
    gen_cc()
    done = gen_menu_items()
    while (not done):
        done = gen_menu_items()
    done = gen_recipes()
    while (not done):
        done = gen_recipes()
    gen_orders()
    done = gen_order_items()
    while (not done):
        done = gen_order_items() #This and the other try until it works statements are a hallmark of how bad this code is! 
    gen_price_change()

populate_db()



