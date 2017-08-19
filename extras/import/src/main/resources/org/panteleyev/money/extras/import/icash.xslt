<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:uuid="java.util.UUID"
                exclude-result-prefixes="uuid">

    <xsl:output indent="yes"/>

    <xsl:template match="/">
        <xsl:element name="Money">
            <xsl:element name="Accounts">
                <xsl:for-each
                        select="Database/DatabaseData/DatabaseUserData/BaseObjectData[@Name='tblAccounts']/Record">
                    <xsl:if test="f[@n='Account_Deleted']='0'">
                        <xsl:element name="Account">
                            <xsl:attribute name="id">
                                <xsl:value-of select="f[@n='RecID']"/>
                            </xsl:attribute>
                            <xsl:element name="accountLimit">
                                <xsl:value-of select="f[@n='Account_Limit']"/>
                            </xsl:element>
                            <xsl:element name="categoryId">
                                <xsl:value-of select="f[@n='Account_Category']"/>
                            </xsl:element>
                            <xsl:element name="comment">
                                <xsl:value-of select="f[@n='Account_Comment']"/>
                            </xsl:element>
                            <xsl:element name="currencyId">
                                <xsl:value-of select="f[@n='Account_CurrencyRecord']"/>
                            </xsl:element>
                            <xsl:element name="currencyRate">
                                <xsl:value-of select="f[@n='Account_CurrencyRate']"/>
                            </xsl:element>
                            <xsl:element name="enabled">
                                <xsl:value-of select="f[@n='Account_Enabled']"/>
                            </xsl:element>
                            <xsl:element name="name">
                                <xsl:value-of select="f[@n='Account_Name']"/>
                            </xsl:element>
                            <xsl:element name="openingBalance">
                                <xsl:value-of select="f[@n='Account_Opening']"/>
                            </xsl:element>
                            <xsl:element name="typeId">
                                <xsl:value-of select="f[@n='Account_Type']"/>
                            </xsl:element>
                            <xsl:element name="guid">
                                <xsl:variable name="uid" select="uuid:randomUUID()"/>
                                <xsl:value-of select="$uid"/>
                            </xsl:element>
                            <xsl:element name="modified">0</xsl:element>
                        </xsl:element>
                    </xsl:if>
                </xsl:for-each>
            </xsl:element>

            <xsl:element name="Categories">
                <xsl:for-each
                        select="Database/DatabaseData/DatabaseUserData/BaseObjectData[@Name='tblCategories']/Record">
                    <xsl:if test="f[@n='Category_Deleted']='0'">
                        <xsl:element name="Category">
                            <xsl:attribute name="id">
                                <xsl:value-of select="f[@n='RecID']"/>
                            </xsl:attribute>
                            <xsl:element name="name">
                                <xsl:value-of select="f[@n='Category_Name']"/>
                            </xsl:element>
                            <xsl:element name="catTypeId">
                                <xsl:value-of select="f[@n='Category_Type']"/>
                            </xsl:element>
                            <xsl:element name="comment">
                                <xsl:value-of select="f[@n='Category_Comment']"/>
                            </xsl:element>
                            <xsl:element name="expanded">
                                <xsl:value-of select="f[@n='Category_Expanded']"/>
                            </xsl:element>
                            <xsl:element name="guid">
                                <xsl:variable name="uid" select="uuid:randomUUID()"/>
                                <xsl:value-of select="$uid"/>
                            </xsl:element>
                            <xsl:element name="modified">0</xsl:element>
                        </xsl:element>
                    </xsl:if>
                </xsl:for-each>
            </xsl:element>

            <xsl:element name="Contacts">
                <xsl:for-each
                        select="Database/DatabaseData/DatabaseUserData/BaseObjectData[@Name='tblContact']/Record">
                    <xsl:if test="f[@n='Contact_Deleted']='0'">
                        <xsl:element name="Contact">
                            <xsl:attribute name="id">
                                <xsl:value-of select="f[@n='RecID']"/>
                            </xsl:attribute>
                            <xsl:element name="name">
                                <xsl:value-of select="f[@n='Contact_Name']"/>
                            </xsl:element>
                            <xsl:element name="comment">
                                <xsl:value-of select="f[@n='Contact_Comment']"/>
                            </xsl:element>
                            <xsl:element name="typeId">
                                <xsl:value-of select="f[@n='Contact_Type']"/>
                            </xsl:element>
                            <xsl:element name="email">
                                <xsl:value-of select="f[@n='Contact_Email']"/>
                            </xsl:element>
                            <xsl:element name="web">
                                <xsl:value-of select="f[@n='Contact_Web']"/>
                            </xsl:element>
                            <xsl:element name="mobile">
                                <xsl:value-of select="f[@n='Contact_Mobile']"/>
                            </xsl:element>
                            <xsl:element name="phone">
                                <xsl:value-of select="f[@n='Contact_Phone']"/>
                            </xsl:element>
                            <xsl:element name="street">
                                <xsl:value-of select="f[@n='Contact_Street']"/>
                            </xsl:element>
                            <xsl:element name="city">
                                <xsl:value-of select="f[@n='Contact_City']"/>
                            </xsl:element>
                            <xsl:element name="country">
                                <xsl:value-of select="f[@n='Contact_Country']"/>
                            </xsl:element>
                            <xsl:element name="zip">
                                <xsl:value-of select="f[@n='Contact_Zip']"/>
                            </xsl:element>
                            <xsl:element name="guid">
                                <xsl:variable name="uid" select="uuid:randomUUID()"/>
                                <xsl:value-of select="$uid"/>
                            </xsl:element>
                            <xsl:element name="modified">0</xsl:element>
                        </xsl:element>
                    </xsl:if>
                </xsl:for-each>
            </xsl:element>

            <xsl:element name="Currencies">
                <xsl:for-each
                        select="Database/DatabaseData/DatabaseUserData/BaseObjectData[@Name='tblCurrency']/Record">
                    <xsl:if test="f[@n='Currency_Deleted']='0'">
                        <xsl:element name="Currency">
                            <xsl:attribute name="id">
                                <xsl:value-of select="f[@n='RecID']"/>
                            </xsl:attribute>
                            <xsl:element name="default">
                                <xsl:value-of select="f[@n='Currency_isDefault']"/>
                            </xsl:element>
                            <xsl:element name="description">
                                <xsl:value-of select="f[@n='Currency_Description']"/>
                            </xsl:element>
                            <xsl:element name="direction">
                                <xsl:value-of select="f[@n='Currency_Direction']"/>
                            </xsl:element>
                            <xsl:element name="formatSymbol">
                                <xsl:value-of select="f[@n='Currency_FormattingSymbol']"/>
                            </xsl:element>
                            <xsl:element name="formatSymbolPosition">
                                <xsl:value-of select="f[@n='Currency_FormattingSymbolPos']"/>
                            </xsl:element>
                            <xsl:element name="rate">
                                <xsl:value-of select="f[@n='Currency_Rate']"/>
                            </xsl:element>
                            <xsl:element name="showFormatSymbol">
                                <xsl:value-of select="f[@n='Currency_ShowFormattingSymbol']"/>
                            </xsl:element>
                            <xsl:element name="symbol">
                                <xsl:value-of select="f[@n='Currency_Symbol']"/>
                            </xsl:element>
                            <xsl:element name="useThousandSeparator">
                                <xsl:value-of select="f[@n='Currency_FormattingTSeparator']"/>
                            </xsl:element>
                            <xsl:element name="guid">
                                <xsl:variable name="uid" select="uuid:randomUUID()"/>
                                <xsl:value-of select="$uid"/>
                            </xsl:element>
                            <xsl:element name="modified">0</xsl:element>
                        </xsl:element>
                    </xsl:if>
                </xsl:for-each>
            </xsl:element>

            <xsl:element name="TransactionGroups">
                <xsl:for-each
                        select="Database/DatabaseData/DatabaseUserData/BaseObjectData[@Name='tblTransGroup']/Record">
                    <xsl:if test="f[@n='TransGroup_Deleted']='0'">
                        <xsl:element name="TransactionGroup">
                            <xsl:attribute name="id">
                                <xsl:value-of select="f[@n='RecID']"/>
                            </xsl:attribute>

                            <xsl:element name="day">
                                <xsl:value-of select="f[@n='TransGroup_Day']"/>
                            </xsl:element>
                            <xsl:element name="expanded">
                                <xsl:value-of select="f[@n='TransGroup_Expanded']"/>
                            </xsl:element>
                            <xsl:element name="month">
                                <xsl:value-of select="f[@n='TransGroup_Month']"/>
                            </xsl:element>
                            <xsl:element name="year">
                                <xsl:value-of select="f[@n='TransGroup_Year']"/>
                            </xsl:element>
                            <xsl:element name="guid">
                                <xsl:variable name="uid" select="uuid:randomUUID()"/>
                                <xsl:value-of select="$uid"/>
                            </xsl:element>
                            <xsl:element name="modified">0</xsl:element>
                        </xsl:element>
                    </xsl:if>
                </xsl:for-each>
            </xsl:element>

            <xsl:element name="Transactions">
                <xsl:for-each
                        select="Database/DatabaseData/DatabaseUserData/BaseObjectData[@Name='tblTransactions']/Record">
                    <xsl:if test="f[@n='Transaction_Deleted']='0'">
                        <xsl:element name="Transaction">
                            <xsl:attribute name="id">
                                <xsl:value-of select="f[@n='RecID']"/>
                            </xsl:attribute>
                            <xsl:element name="accountCreditedCategoryId">
                                <xsl:value-of select="f[@n='Transaction_AccountCreditedCate']"/>
                            </xsl:element>
                            <xsl:element name="accountCreditedId">
                                <xsl:value-of select="f[@n='Transaction_AccountCredited']"/>
                            </xsl:element>
                            <xsl:element name="accountCreditedTypeId">
                                <xsl:value-of select="f[@n='Transaction_AccountCreditedType']"/>
                            </xsl:element>
                            <xsl:element name="accountDebitedCategoryId">
                                <xsl:value-of select="f[@n='Transaction_AccountDebitedCate']"/>
                            </xsl:element>
                            <xsl:element name="accountDebitedId">
                                <xsl:value-of select="f[@n='Transaction_AccountDebited']"/>
                            </xsl:element>
                            <xsl:element name="accountDebitedTypeId">
                                <xsl:value-of select="f[@n='Transaction_AccountDebitedType']"/>
                            </xsl:element>
                            <xsl:element name="amount">
                                <xsl:value-of select="f[@n='Transaction_Amount']"/>
                            </xsl:element>
                            <xsl:element name="checked">
                                <xsl:value-of select="f[@n='Transaction_Checked']"/>
                            </xsl:element>
                            <xsl:element name="comment">
                                <xsl:value-of select="f[@n='Transaction_Comment']"/>
                            </xsl:element>
                            <xsl:element name="contactId">
                                <xsl:value-of select="f[@n='Transaction_issuing']"/>
                            </xsl:element>
                            <xsl:element name="day">
                                <xsl:value-of select="f[@n='Transaction_Day']"/>
                            </xsl:element>
                            <xsl:element name="groupId">
                                <xsl:value-of select="f[@n='Transaction_GroupID']"/>
                            </xsl:element>
                            <xsl:element name="invoiceNumber">
                                <xsl:value-of select="f[@n='Transaction_InvoiceNum']"/>
                            </xsl:element>
                            <xsl:element name="month">
                                <xsl:value-of select="f[@n='Transaction_Month']"/>
                            </xsl:element>
                            <xsl:element name="rate">
                                <xsl:value-of select="f[@n='Transaction_CurrencyRate']"/>
                            </xsl:element>
                            <xsl:element name="rateDirection">
                                <xsl:value-of select="f[@n='Transaction_CurrencyDirection1']"/>
                            </xsl:element>
                            <xsl:element name="transactionTypeId">
                                <xsl:variable name="trType" select="f[@n='Transaction_Type']"/>
                                <xsl:choose>
                                    <xsl:when test="$trType='0'">21</xsl:when>
                                    <xsl:otherwise><xsl:value-of select="$trType"/></xsl:otherwise>
                                </xsl:choose>
                            </xsl:element>
                            <xsl:element name="year">
                                <xsl:value-of select="f[@n='Transaction_Year']"/>
                            </xsl:element>
                            <xsl:element name="guid">
                                <xsl:variable name="uid" select="uuid:randomUUID()"/>
                                <xsl:value-of select="$uid"/>
                            </xsl:element>
                            <xsl:element name="modified">0</xsl:element>
                        </xsl:element>
                    </xsl:if>
                </xsl:for-each>
            </xsl:element>

        </xsl:element>
    </xsl:template>
</xsl:stylesheet>