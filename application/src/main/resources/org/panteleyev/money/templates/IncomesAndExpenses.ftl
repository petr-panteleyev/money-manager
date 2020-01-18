<html>
<head>
<meta charset="utf-8">
<title>Incomes and Expenses</title>
</head>

<body>
<h1>Incomes and Expenses</h1>

<h2>Summary</h2>
<table border = '0'>
<tr><td>Expenses:<td>${expensesSum}
<tr><td>Incomes:<td>${incomesSum}
<tr><td>Balance:<td>${balanceSum}
</table>

<h2>Details</h2>

<table border='1' cellspacing='1' cellpadding='5'>

<tr><th colspan = '4'>Expenses

<tr><th>Category<th>Account<th>Counterparty<th>Sum

<#list expenses as category>
<tr><td align='left' colspan='3'>${category.text}<td align='right'>${category.amount}
    <#list category.items as account>
    <tr><td>&nbsp;<td alight='left' colspan='2'>${account.text}<td align='right'>${account.amount}
        <#list account.items as contact>
        <tr><td>&nbsp;<td>&nbsp;<td align='left'>${contact.text}<td align='right'>${account.amount}
        </#list>
    </#list>
</#list>

<tr><th colspan = '4'>Incomes

<tr><th>Category<th>Account<th>Counterparty<th>Sum

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