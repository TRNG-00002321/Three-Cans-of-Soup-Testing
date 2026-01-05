import csv

# Configuration
filename = "users.csv"
number_of_users = 1000
base_username = "employee"
base_password = "password123"

print(f"Generating {number_of_users} users...")

with open(filename, mode='w', newline='') as file:
    writer = csv.writer(file)
    # Header row (optional in JMeter, but good for readability)
    writer.writerow(["username", "password"])
    
    for i in range(1, number_of_users + 1):
        # Creates employee1, employee2, ... employee1000
        username = f"{base_username}{i}"
        writer.writerow([username, base_password])

print(f"Done! '{filename}' created successfully.")