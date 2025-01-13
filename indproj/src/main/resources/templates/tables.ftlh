<!DOCTYPE html>
<html>
<head>
    <title>Tables</title>
</head>
<body>
<h1>Available Tables</h1>
<ul>
    <#list tables as table>
        <li><a href="/table/${table?lower_case}">${table}</a></li>
    </#list>
</ul>
</body>
</html>
