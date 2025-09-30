#!/bin/bash
set -e

ENDPOINT="http://dynamodb-local:8000"

echo "⏳ Esperando a DynamoDB Local en $ENDPOINT ..."
until aws dynamodb list-tables --endpoint-url $ENDPOINT > /dev/null 2>&1; do
  sleep 2
done
echo "✅ DynamoDB Local disponible."

echo "📌 Inicializando tablas en DynamoDB Local ($ENDPOINT)..."

# ------------------------
# Crear tablas
# ------------------------
aws dynamodb create-table \
  --table-name Accounts \
  --attribute-definitions AttributeName=accountId,AttributeType=S \
  --key-schema AttributeName=accountId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url $ENDPOINT || echo "⚠️ Tabla Accounts ya existe"

aws dynamodb create-table \
  --table-name Funds \
  --attribute-definitions AttributeName=fundId,AttributeType=S \
  --key-schema AttributeName=fundId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url $ENDPOINT || echo "⚠️ Tabla Funds ya existe"

aws dynamodb create-table \
  --table-name Transactions \
  --attribute-definitions AttributeName=transactionId,AttributeType=S AttributeName=accountId,AttributeType=S AttributeName=createdAt,AttributeType=S \
  --key-schema AttributeName=transactionId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --global-secondary-indexes '[
    {
      "IndexName": "accountId-index",
      "KeySchema": [
        {"AttributeName":"accountId","KeyType":"HASH"},
        {"AttributeName":"createdAt","KeyType":"RANGE"}
      ],
      "Projection":{"ProjectionType":"ALL"}
    }
  ]' \
  --endpoint-url $ENDPOINT || echo "⚠️ Tabla Transactions ya existe"

aws dynamodb create-table \
  --table-name Subscriptions \
  --attribute-definitions AttributeName=subscriptionId,AttributeType=S AttributeName=accountId,AttributeType=S \
  --key-schema AttributeName=subscriptionId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --global-secondary-indexes '[
    {
      "IndexName": "accountId-index",
      "KeySchema": [
        {"AttributeName":"accountId","KeyType":"HASH"}
      ],
      "Projection":{"ProjectionType":"ALL"}
    }
  ]' \
  --endpoint-url $ENDPOINT || echo "⚠️ Tabla Subscriptions ya existe"

echo "✅ Tablas listas."

# ------------------------
# Seeds: Cuentas
# ------------------------
echo "📌 Insertando cuenta de prueba..."
aws dynamodb put-item \
  --table-name Accounts \
  --item '{
    "accountId": {"S":"acct-0001"},
    "name": {"S":"Cuenta Única de Prueba"},
    "email": {"S":"test@example.com"},
    "balance": {"N":"500000"}
  }' \
  --endpoint-url $ENDPOINT || echo "⚠️ No se pudo insertar cuenta"

# ------------------------
# Seeds: Fondos
# ------------------------
echo "📌 Insertando fondos iniciales..."
aws dynamodb batch-write-item \
  --request-items '{
    "Funds": [
      {
        "PutRequest": {
          "Item": {"fundId":{"S":"1"},"name":{"S":"FPV_BTG_PACTUAL_RECAUDADORA"},"minAmount":{"N":"75000"},"category":{"S":"FPV"},"active":{"BOOL":true}}
        }
      },
      {
        "PutRequest": {
          "Item": {"fundId":{"S":"2"},"name":{"S":"FPV_BTG_PACTUAL_ECOPETROL"},"minAmount":{"N":"125000"},"category":{"S":"FPV"},"active":{"BOOL":true}}
        }
      },
      {
        "PutRequest": {
          "Item": {"fundId":{"S":"3"},"name":{"S":"DEUDAPRIVADA"},"minAmount":{"N":"50000"},"category":{"S":"FIC"},"active":{"BOOL":true}}
        }
      },
      {
        "PutRequest": {
          "Item": {"fundId":{"S":"4"},"name":{"S":"FDO_ACCIONES"},"minAmount":{"N":"250000"},"category":{"S":"FIC"},"active":{"BOOL":true}}
        }
      },
      {
        "PutRequest": {
          "Item": {"fundId":{"S":"5"},"name":{"S":"FPV_BTG_PACTUAL_DINAMICA"},"minAmount":{"N":"100000"},"category":{"S":"FPV"},"active":{"BOOL":true}}
        }
      }
    ]
  }' \
  --endpoint-url $ENDPOINT || echo "⚠️ No se pudieron insertar fondos"

echo "✅ Seeds completados."
