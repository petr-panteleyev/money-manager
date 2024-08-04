<?xml version="1.0" encoding="utf-8" ?>
<!--
  Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
  SPDX-License-Identifier: BSD-2-Clause
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/StatementReportRecords">
        <html lang='ru'>
            <head>
                <title>Выписка</title>
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
                        <th>Дата</th>
                        <th>Дата исп.</th>
                        <th>Описание</th>
                        <th>Сумма</th>
                    </tr>
                    <xsl:for-each select="StatementReportRecord">
                        <tr>
                            <td>
                                <xsl:value-of select="@date"/>
                            </td>
                            <td>
                                <xsl:value-of select="@executionDate"/>
                            </td>
                            <td>
                                <xsl:value-of select="@description"/>
                            </td>
                            <td class="amount">
                                <xsl:value-of select="@amount"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>