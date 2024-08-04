<?xml version="1.0" encoding="utf-8" ?>
<!--
  Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
  SPDX-License-Identifier: BSD-2-Clause
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/AccountReportRecords">
        <html lang='ru'>
            <head>
                <title>Счета</title>
                <style>
                    table, th, td {
                        border: 1px solid black;
                        border-spacing: 0;
                    }

                    th, td {
                        padding: 5;
                        font-size: small;
                    }

                    td.amount {
                        text-align: right;
                    }
                </style>
            </head>
            <body>
                <table>
                    <tr>
                        <th>Название</th>
                        <th>Категория</th>
                        <th>Валюта</th>
                        <th>%%</th>
                        <th>До</th>
                        <th>Комментарий</th>
                        <th>Баланс</th>
                    </tr>
                    <xsl:for-each select="AccountReportRecord">
                        <tr>
                            <td>
                                <xsl:value-of select="@name"/>
                            </td>
                            <td>
                                <xsl:value-of select="@category"/>
                            </td>
                            <td>
                                <xsl:value-of select="@currency"/>
                            </td>
                            <td>
                                <xsl:value-of select="@interest"/>
                            </td>
                            <td>
                                <xsl:value-of select="@expiration"/>
                            </td>
                            <td>
                                <xsl:value-of select="@comment"/>
                            </td>
                            <td class="amount">
                                <xsl:value-of select="@balance"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>