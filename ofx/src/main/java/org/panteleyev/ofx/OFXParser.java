/*
 Copyright © 2020-2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class OFXParser {
    private static final String OFX_PI = "OFX";

    private static final DateTimeFormatter OFX_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String BANK_MESSAGE_ROOT_EXPR = "/OFX/BANKMSGSRSV1";
    private static final String CREDIT_CARD_ROOT_EXPR = "/OFX/CREDITCARDMSGSRSV1";

    public OFXStatement parse(InputStream is) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            var documentBuilder = factory.newDocumentBuilder();
            var doc = documentBuilder.parse(is);

            var header = parseHeader(doc);
            if (header == null) {
                throw new OFXParserException("No header");
            }

            // Parse credit card statement message
            var xPathFactory = XPathFactory.newInstance();
            var xPath = xPathFactory.newXPath();

            var bankStatement = new ArrayList<AccountStatementList>();
            var creditCardStatement = new ArrayList<CreditCardStatementList>();

            // Parse bank message
            //
            var bankRootExpr = xPath.compile(BANK_MESSAGE_ROOT_EXPR);
            var bankRoot = (Element) bankRootExpr.evaluate(doc, XPathConstants.NODE);
            if (bankRoot != null) {
                var statementResponseNodes = bankRoot.getElementsByTagName("STMTTRNRS");
                for (int i = 0; i < statementResponseNodes.getLength(); i++) {
                    var statementResponseNode = (Element) statementResponseNodes.item(i);
                    var transactionResponseList = statementResponseNode.getElementsByTagName("STMTRS");
                    var statementResponseList = new ArrayList<AccountStatement>();

                    for (int j = 0; j < transactionResponseList.getLength(); j++) {
                        var transactionResponseNode = transactionResponseList.item(j);

                        // Currency
                        var currencyExpr = xPath.compile("CURDEF");
                        var currency = (String) currencyExpr.evaluate(transactionResponseNode, XPathConstants.STRING);

                        var accountExpr = xPath.compile("BANKACCTFROM");
                        var accountNode = (Node) accountExpr.evaluate(transactionResponseNode, XPathConstants.NODE);
                        var accountInfo = parseAccount(accountNode);

                        var bankTransactionListNodeExpr = xPath.compile("BANKTRANLIST");
                        var bankTransactionListNode =
                                (Node) bankTransactionListNodeExpr.evaluate(transactionResponseNode,
                                        XPathConstants.NODE);
                        var bankTransactionList = parseBankTransactionList(bankTransactionListNode);

                        var statementResponse = new AccountStatement(currency, accountInfo,
                                bankTransactionList);
                        statementResponseList.add(statementResponse);
                    }

                    bankStatement.add(new AccountStatementList(statementResponseList));
                }
            }

            // Parse credit card statement message
            //
            var ccRootExpr = xPath.compile(CREDIT_CARD_ROOT_EXPR);
            var creditCardRoot = (Element) ccRootExpr.evaluate(doc, XPathConstants.NODE);
            if (creditCardRoot != null) {
                var statementResponseNodes = creditCardRoot.getElementsByTagName("CCSTMTTRNRS");
                for (int i = 0; i < statementResponseNodes.getLength(); i++) {
                    var statementResponseNode = (Element) statementResponseNodes.item(i);
                    var transactionResponseList = statementResponseNode.getElementsByTagName("CCSTMTRS");
                    var creditCardStatementResponseList = new ArrayList<CreditCardStatement>();

                    for (int j = 0; j < transactionResponseList.getLength(); j++) {
                        var transactionResponseNode = transactionResponseList.item(j);

                        // Currency
                        var currencyExpr = xPath.compile("CURDEF");
                        var currency = (String) currencyExpr.evaluate(transactionResponseNode, XPathConstants.STRING);

                        // Account
                        var accountExpr = xPath.compile("CCACCTFROM");
                        var accountNode = (Node) accountExpr.evaluate(transactionResponseNode, XPathConstants.NODE);
                        var accountInfo = parseAccount(accountNode);

                        var bankTransactionListNodeExpr = xPath.compile("BANKTRANLIST");
                        var bankTransactionListNode =
                                (Node) bankTransactionListNodeExpr.evaluate(transactionResponseNode,
                                        XPathConstants.NODE);
                        var bankTransactionList = parseBankTransactionList(bankTransactionListNode);

                        // TODO: parse pending transaction list
                        var pendingTransactionListNodeExpr = xPath.compile("BANKTRANLISTP");
                        var pendingTransactionListNode =
                                (Node) pendingTransactionListNodeExpr.evaluate(transactionResponseNode,
                                        XPathConstants.NODE);
                        var pendingTransactionList = parsePendingTransactionList(pendingTransactionListNode);

                        var statementResponse = new CreditCardStatement(currency, accountInfo,
                                bankTransactionList,
                                pendingTransactionList);
                        creditCardStatementResponseList.add(statementResponse);
                    }

                    creditCardStatement.add(new CreditCardStatementList(creditCardStatementResponseList));
                }
            }

            return new OFXStatement(header, bankStatement, creditCardStatement);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private BankTransactionList parseBankTransactionList(Node bankTransactionListNode) throws Exception {
        if (bankTransactionListNode == null) {
            return new BankTransactionList();
        }

        var xPathFactory = XPathFactory.newInstance();
        var xPath = xPathFactory.newXPath();

        // Dates
        var dateStartExpr = xPath.compile("DTSTART");
        var dateStartString = (String) dateStartExpr.evaluate(bankTransactionListNode,
                XPathConstants.STRING);
        var dateEndExpr = xPath.compile("DTEND");
        var dateEndString = (String) dateEndExpr.evaluate(bankTransactionListNode,
                XPathConstants.STRING);

        // Transacitons
        var trList = new ArrayList<StatementTransaction>();

        var transExpr = xPath.compile("STMTTRN");
        var transactionList = (NodeList) transExpr.evaluate(bankTransactionListNode, XPathConstants.NODESET);
        if (transactionList != null) {
            for (int k = 0; k < transactionList.getLength(); k++) {
                var transaction = (Element) transactionList.item(k);
                trList.add(parseStatementTransaction(transaction));
            }
        }

        return new BankTransactionList(
                LocalDateTime.parse(dateStartString, OFX_DATE_FORMAT),
                LocalDateTime.parse(dateEndString, OFX_DATE_FORMAT),
                trList);
    }

    private AccountInfo parseAccount(Node accountNode) {
        String accountId = null;
        String bankId = null;
        var type = AccountInfo.Type.NONE;

        var children = accountNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            var item = children.item(i);
            if (!(item instanceof Element field)) {
                continue;
            }

            if ("ACCTID".equals(field.getTagName())) {
                accountId = field.getTextContent();
            }

            if ("BANKID".equals(field.getTagName())) {
                bankId = field.getTextContent();
            }

            if ("ACCTTYPE".equals(field.getTagName())) {
                type = AccountInfo.Type.valueOf(field.getTextContent());
            }
        }

        return new AccountInfo(type, bankId, accountId);
    }

    private PendingTransactionList parsePendingTransactionList(Node pendingTransactionListNode) throws Exception {
        if (pendingTransactionListNode == null) {
            return new PendingTransactionList();
        }

        var xPathFactory = XPathFactory.newInstance();
        var xPath = xPathFactory.newXPath();

        // Dates
        var dateAsOfExpr = xPath.compile("DTASOF");
        var dateAsOfString = (String) dateAsOfExpr.evaluate(pendingTransactionListNode,
                XPathConstants.STRING);

        // Transacitons
        var trList = new ArrayList<PendingTransaction>();

        var transExpr = xPath.compile("STMTTRNP");
        var transactionList = (NodeList) transExpr.evaluate(pendingTransactionListNode, XPathConstants.NODESET);
        if (transactionList != null) {
            for (int k = 0; k < transactionList.getLength(); k++) {
                var transaction = (Element) transactionList.item(k);
                trList.add(parsePendingTransaction(transaction));
            }
        }

        return new PendingTransactionList(
                LocalDateTime.parse(dateAsOfString, OFX_DATE_FORMAT),
                trList);
    }

    private StatementTransaction parseStatementTransaction(Element transaction) {
        var children = transaction.getChildNodes();

        var builder = new StatementTransaction.Builder();
        for (int n = 0; n < children.getLength(); n++) {
            var item = children.item(n);
            if (!(item instanceof Element field)) {
                continue;
            }

            switch (field.getTagName()) {
                case "TRNTYPE" -> builder.type(field.getTextContent());
                case "NAME" -> builder.name(field.getTextContent());
                case "MEMO" -> builder.memo(field.getTextContent());
                case "DTPOSTED" -> builder.datePosted(LocalDateTime.parse(field.getTextContent(), OFX_DATE_FORMAT));
                case "DTAVAIL" -> builder.dateAvailable(LocalDateTime.parse(field.getTextContent(), OFX_DATE_FORMAT));
                case "DTUSER" -> builder.dateUser(LocalDateTime.parse(field.getTextContent(), OFX_DATE_FORMAT));
                case "TRNAMT" -> builder.amount(field.getTextContent());
                case "CHECKNUM" -> builder.checkNum(field.getTextContent());
            }
        }

        return builder.build();
    }

    private PendingTransaction parsePendingTransaction(Element transaction) {
        var children = transaction.getChildNodes();

        var builder = new PendingTransaction.Builder();
        for (int n = 0; n < children.getLength(); n++) {
            var item = children.item(n);
            if (!(item instanceof Element field)) {
                continue;
            }

            switch (field.getTagName()) {
                case "TRNTYPE" -> builder.type(field.getTextContent());
                case "NAME" -> builder.name(field.getTextContent());
                case "MEMO" -> builder.memo(field.getTextContent());
                case "DTTRAN" -> builder.dateTransaction(LocalDateTime.parse(field.getTextContent(), OFX_DATE_FORMAT));
                case "DTEXPIRE" -> builder.dateExpire(LocalDateTime.parse(field.getTextContent(), OFX_DATE_FORMAT));
                case "TRNAMT" -> builder.amount(field.getTextContent());
            }
        }

        return builder.build();
    }

    private Header parseHeader(Document doc) {
        var rootChildren = doc.getChildNodes();
        for (int i = 0; i < rootChildren.getLength(); i++) {
            var node = rootChildren.item(i);
            var nodeName = node.getNodeName();

            if (!OFX_PI.equals(nodeName) || node.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
                continue;
            }

            var ofx = (ProcessingInstruction) node;
            var data = ofx.getData();
            var attributes = data.split(" ");

            var attrMap = new HashMap<String, String>();
            for (var attr : attributes) {
                var nameValue = attr.trim().split("=");
                if (nameValue.length != 2) {
                    throw new OFXParserException("Invalid OFX processing instruction");
                }

                var name = nameValue[0];
                var value = nameValue[1];

                attrMap.put(name, value.substring(1, value.length() - 1));
            }

            return new Header(attrMap.get("OFXHEADER"), attrMap.get("VERSION"), attrMap.get("SECURITY"),
                    attrMap.get("OLDFILEUID"), attrMap.get("OLDFILEUID"));
        }

        return null;
    }
}
