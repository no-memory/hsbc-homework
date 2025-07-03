#!/bin/bash

# Transaction Management System Demo Script
# This script demonstrates the key features of the transaction management API

BASE_URL="http://localhost:8080/api/v1/transactions"
HEALTH_URL="http://localhost:8080/actuator/health"

echo "🏦 Transaction Management System Demo"
echo "===================================="
echo

# Check if the application is running
echo "📡 Checking application health..."
if curl -sf "$HEALTH_URL" > /dev/null; then
    echo "✅ Application is running!"
else
    echo "❌ Application is not running. Please start it with: ./mvnw spring-boot:run"
    exit 1
fi
echo

# Function to make API calls with pretty output
make_api_call() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo "🔄 $description"
    echo "   $method $url"
    
    if [ -n "$data" ]; then
        echo "   Data: $data"
        response=$(curl -s -X "$method" -H "Content-Type: application/json" -d "$data" "$url")
    else
        response=$(curl -s -X "$method" "$url")
    fi
    
    echo "   Response: $response"
    echo
    
    # Extract ID from response if it's a POST request
    if [ "$method" = "POST" ] && echo "$response" | grep -q '"id"'; then
        echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2
    fi
}

# Clean up any existing transactions
echo "🧹 Cleaning up existing transactions..."
curl -s -X DELETE "$BASE_URL" > /dev/null
echo

# Create sample transactions
echo "💰 Creating sample transactions..."
echo

# Credit transactions
transaction1_id=$(make_api_call "POST" "$BASE_URL" '{
    "accountNumber": "1234567890",
    "amount": 1500.50,
    "type": "CREDIT",
    "description": "Salary deposit",
    "reference": "SAL-2024-001"
}' "Creating salary deposit")

transaction2_id=$(make_api_call "POST" "$BASE_URL" '{
    "accountNumber": "1234567890",
    "amount": 250.00,
    "type": "CREDIT",
    "description": "Bonus payment",
    "reference": "BON-2024-001"
}' "Creating bonus payment")

# Debit transactions
transaction3_id=$(make_api_call "POST" "$BASE_URL" '{
    "accountNumber": "1234567890",
    "amount": 89.99,
    "type": "DEBIT",
    "description": "Online shopping",
    "reference": "SHOP-2024-001"
}' "Creating online shopping debit")

transaction4_id=$(make_api_call "POST" "$BASE_URL" '{
    "accountNumber": "0987654321",
    "amount": 2500.00,
    "type": "WITHDRAWAL",
    "description": "Cash withdrawal",
    "reference": "ATM-2024-001"
}' "Creating cash withdrawal")

# Transfer transaction
transaction5_id=$(make_api_call "POST" "$BASE_URL" '{
    "accountNumber": "0987654321",
    "amount": 500.00,
    "type": "TRANSFER",
    "description": "Transfer to savings",
    "reference": "TXF-2024-001"
}' "Creating transfer transaction")

echo "📊 Demonstrating query operations..."
echo

# Get all transactions (paginated)
make_api_call "GET" "$BASE_URL?page=0&size=10" "" "Getting all transactions (first page)"

# Get transaction by ID
if [ -n "$transaction1_id" ]; then
    make_api_call "GET" "$BASE_URL/$transaction1_id" "" "Getting transaction by ID: $transaction1_id"
fi

# Get transactions by account number
make_api_call "GET" "$BASE_URL/account/1234567890" "" "Getting transactions for account 1234567890"

# Get transactions by type
make_api_call "GET" "$BASE_URL/type/CREDIT" "" "Getting all CREDIT transactions"

# Get transactions by amount range
make_api_call "GET" "$BASE_URL/amount-range?minAmount=100&maxAmount=1000" "" "Getting transactions between \$100 and \$1000"

# Get transactions by date range (last hour)
current_time=$(date -u +"%Y-%m-%dT%H:%M:%S")
one_hour_ago=$(date -u -d '1 hour ago' +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -v-1H +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || echo "2024-01-01T00:00:00")
make_api_call "GET" "$BASE_URL/date-range?startDate=${one_hour_ago}&endDate=${current_time}" "" "Getting transactions from last hour"

echo "📈 Demonstrating statistics operations..."
echo

# Get transaction count
make_api_call "GET" "$BASE_URL/statistics/count" "" "Getting total transaction count"

# Get count by type
make_api_call "GET" "$BASE_URL/statistics/count-by-type" "" "Getting transaction count by type"

# Get count by account
make_api_call "GET" "$BASE_URL/statistics/count-by-account" "" "Getting transaction count by account"

# Get total amount
make_api_call "GET" "$BASE_URL/statistics/total-amount" "" "Getting total transaction amount"

# Get total amount by type
make_api_call "GET" "$BASE_URL/statistics/total-amount-by-type/CREDIT" "" "Getting total amount for CREDIT transactions"

# Get total amount by account
make_api_call "GET" "$BASE_URL/statistics/total-amount-by-account/1234567890" "" "Getting total amount for account 1234567890"

echo "✏️ Demonstrating update operation..."
echo

# Update a transaction
if [ -n "$transaction1_id" ]; then
    make_api_call "PUT" "$BASE_URL/$transaction1_id" '{
        "accountNumber": "1234567890",
        "amount": 1600.00,
        "type": "CREDIT",
        "description": "Updated salary deposit",
        "reference": "SAL-2024-001-UPD"
    }' "Updating transaction $transaction1_id"
fi

echo "🗑️ Demonstrating delete operation..."
echo

# Delete a transaction
if [ -n "$transaction5_id" ]; then
    make_api_call "DELETE" "$BASE_URL/$transaction5_id" "" "Deleting transaction $transaction5_id"
fi

# Verify deletion
make_api_call "GET" "$BASE_URL/statistics/count" "" "Getting transaction count after deletion"

echo "❌ Demonstrating error handling..."
echo

# Try to create invalid transaction (negative amount)
make_api_call "POST" "$BASE_URL" '{
    "accountNumber": "123",
    "amount": -100.00,
    "type": "CREDIT",
    "description": ""
}' "Creating invalid transaction (should fail)"

# Try to get non-existent transaction
make_api_call "GET" "$BASE_URL/99999" "" "Getting non-existent transaction (should fail)"

# Try to update non-existent transaction
make_api_call "PUT" "$BASE_URL/99999" '{
    "accountNumber": "1234567890",
    "amount": 100.00,
    "type": "CREDIT",
    "description": "Test"
}' "Updating non-existent transaction (should fail)"

echo "🔍 Additional endpoints to explore:"
echo "   • Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   • Health Check: http://localhost:8080/actuator/health"
echo "   • Metrics: http://localhost:8080/actuator/metrics"
echo "   • Cache Info: http://localhost:8080/actuator/cache"
echo

echo "✅ Demo completed successfully!"
echo "   Created sample transactions and demonstrated all major API features."
echo "   The application is ready for production use!"
echo 