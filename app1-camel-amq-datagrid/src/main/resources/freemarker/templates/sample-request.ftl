{
"id": ${body},
"attribute": "FARM",
"appId": "APP1",
"loans": [
{
"status": "approved",
"value": ${headers.RANDOM_AMOUNT}
}
]
,
"flocks": [
{
"type": "APP1 - Chicken",
"location": "Grange",
"total": ${headers.RANDOM_COUNT}
}
]
}