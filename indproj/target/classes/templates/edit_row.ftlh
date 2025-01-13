<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit ${tableName}</title>
    <style>
        body {
            font-family: Arial, sans-serif;
        }
        .error {
            color: red;
            margin-bottom: 10px;
            font-weight: bold;
            padding: 10px;
            background-color: #ffe6e6;
            border: 1px solid #ff0000;
            border-radius: 5px;
        }
    </style>
</head>
<body>
    <h1>Edit ${tableName} - ID: ${entity.id}</h1>

    <#if error??>
        <div class="error">${error}</div>
    </#if>

    <form action="/table/${tableName}/edit/${id}" method="post">
        <#list entity?keys as field>
            <#if field != "id">
            <div>
                <label for="${field}">${field}</label>
                <#if field?ends_with("_id")>
                    <input type="number" id="${field}" name="${field}" value="${entity[field]!}" required>
                <#else>
                    <input type="text" id="${field}" name="${field}" value="${entity[field]!}" required>
                </#if>
            </div>
            </#if>
        </#list>
        <button type="submit">Save</button>
    </form>


    <a href="/table/${tableName}">Back to ${tableName}</a>
</body>
</html>