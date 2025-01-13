<!DOCTYPE html>
<html>
<head>
    <title>${tableName?capitalize}</title>
    <style>
        table {
            border-collapse: collapse;
            width: 100%;
            margin-top: 20px;
        }
        th, td {
            border: 1px solid black;
            padding: 8px;
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
        }
        h1 {
            text-align: center;
            margin-top: 20px;
        }
        a, button {
            text-decoration: none;
            color: blue;
            cursor: pointer;
        }
        button {
            border: none;
            background: none;
            color: red;
        }
    </style>
</head>
<body>
    <h1>Table: ${tableName?capitalize}</h1>

    <table>
        <thead>
            <tr>
                <#list headers as header>
                    <th>${header?capitalize}</th>
                </#list>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <#list rows as row>
                <tr>
                    <#list row as cell>
                        <td>${cell}</td>
                    </#list>
                    <td>
                        <a href="/table/${tableName}/edit/${row[0]}">Edit</a>
                        <form action="/table/${tableName}/delete/${row[0]}" method="post" style="display: inline;">
                            <button type="submit">Delete</button>
                        </form>
                    </td>
                </tr>
            </#list>
        </tbody>
    </table>

    <#if errorMessage??>
        <div id="errorPopup" style="color: red; border: 1px solid red; padding: 10px; margin: 10px 0;">
            <strong>Error:</strong> ${errorMessage}
            <button onclick="document.getElementById('errorPopup').style.display='none'">Close</button>
        </div>
    </#if>
    <br>
    <h3> ADD A RECORD </h3>

    <form action="/table/${tableName}/add" method="post">
        <table>
            <tbody>
                <#list headers as header>
                    <#if header != "id">
                        <tr>
                            <td><label for="${header}">${header}</label></td>
                            <td><input type="text" name="${header}" id="${header}" required></td>
                        </tr>
                    </#if>
                </#list>
            </tbody>
        </table>
        <button type="submit">Add</button>
    </form>
    <br>
    <a href="/">Back to Table List</a>
</body>
</html>


