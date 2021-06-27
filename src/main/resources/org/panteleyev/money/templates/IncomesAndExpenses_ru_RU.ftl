<html lang='ru'>
<head>
<meta charset="utf-8">
<title>Доходы и расходы</title>
</head>

<body>
<h1>Доходы и расходы</h1>

<h2>Сводка</h2>
<table border = '0'>
<tr><td>Расходы:<td>${expensesSum}
<tr><td>Доходы:<td>${incomesSum}
<tr><td>Баланс:<td>${balanceSum}
</table>

<h2>Детали</h2>

<table border='1' cellspacing='1' cellpadding='5'>

<tr><th colspan = '4'>Расходы

<tr><th>Категория<th>Счет<th>Контрагент<th>Сумма

<#list expenses as category>
<tr><td align='left' colspan='3'>${category.text}<td align='right'>${category.amount}
    <#list category.items as account>
    <tr><td>&nbsp;<td alight='left' colspan='2'>${account.text}<td align='right'>${account.amount}
        <#list account.items as contact>
        <tr><td>&nbsp;<td>&nbsp;<td align='left'>${contact.text}<td align='right'>${account.amount}
        </#list>
    </#list>
</#list>

<tr><th colspan = '4'>Доходы

<tr><th>Категория<th>Счет<th>Контрагент<th>Сумма

<#list incomes as category>
<tr><td align='left' colspan='3'>${category.text}<td align='right'>${category.amount}
    <#list category.items as account>
    <tr><td>&nbsp;<td alight='left' colspan='2'>${account.text}<td align='right'>${account.amount}
        <#list account.items as contact>
        <tr><td>&nbsp;<td>&nbsp;<td align='left'>${contact.text}<td align='right'>${account.amount}
        </#list>
    </#list>
</#list>

</table>

</body>
</html>