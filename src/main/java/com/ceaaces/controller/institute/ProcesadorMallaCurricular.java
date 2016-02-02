package ec.gob.ceaaces.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

public class ProcesadorMallaCurricular {

    private static final Logger LOG = Logger
	    .getLogger(ProcesadorMallaCurricular.class.getSimpleName());

    public WordprocessingMLPackage getTemplate() throws Docx4JException,
	    FileNotFoundException {
	WordprocessingMLPackage template = null;
	try {
	    template = WordprocessingMLPackage.load(new FileInputStream(
		    new File(getClass().getResource("/mallacurricular.docx")
		            .toURI())));
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return template;
    }

    public static List<Object> getAllElementFromObject(Object obj,
	    Class<?> toSearch) {
	List<Object> result = new ArrayList<Object>();
	if (obj instanceof JAXBElement)
	    obj = ((JAXBElement<?>) obj).getValue();

	if (obj.getClass().equals(toSearch))
	    result.add(obj);
	else if (obj instanceof ContentAccessor) {
	    List<?> children = ((ContentAccessor) obj).getContent();
	    for (Object child : children) {
		result.addAll(getAllElementFromObject(child, toSearch));
	    }

	}
	return result;
    }

    public void replacePlaceholder(WordprocessingMLPackage template,
	    String name, String placeholder) {
	List<Object> texts = getAllElementFromObject(
	        template.getMainDocumentPart(), Text.class);

	for (Object text : texts) {
	    Text textElement = (Text) text;
	    if (textElement.getValue().equals(placeholder)) {
		textElement.setValue(name);
	    }
	}
    }

    public void writeDocxToStream(WordprocessingMLPackage template,
	    String target) throws IOException, Docx4JException {
	File f = new File(target);
	template.save(f);
    }

    public void replaceParagraph(String placeholder, String textToAdd,
	    WordprocessingMLPackage template, ContentAccessor addTo) {
	int index = 0;
	int contador = 0;
	// 1. get the paragraph
	List<Object> paragraphs = getAllElementFromObject(
	        template.getMainDocumentPart(), P.class);

	P toReplace = null;
	int z = 0;

	for (Object p : paragraphs) {
	    List<Object> texts = getAllElementFromObject(p, Text.class);
	    int contlist = 0;
	    for (Object t : texts) {

		Text content = (Text) t;
		if (content.getValue().equals(placeholder)) {
		    contador = contlist;

		    index = z;
		    toReplace = (P) p;
		    break;

		}
		contlist++;

	    }
	    z++;
	}

	// we now have the paragraph that contains our placeholder: toReplace
	// 2. split into seperate lines

	String ptext = textToAdd;

	// 3. copy the found paragraph to keep styling correct
	P copy = XmlUtils.deepCopy(toReplace);

	// replace the text elements from the copy
	List texts = getAllElementFromObject(copy, Text.class);
	if (texts.size() > 0) {
	    Text textToReplace = (Text) texts.get(contador);
	    textToReplace.setValue(ptext);
	}

	// add the paragraph to the document
	addTo.getContent().add(index, copy);
	addTo.getContent().remove(index + 1);
	// // 4. remove the original one
	// ((ContentAccessor) toReplace.getParent()).getContent()
	// .remove(toReplace);

    }

    public void replaceTable(String[] placeholders,
	    List<Map<String, String>> textToAdd,
	    WordprocessingMLPackage template) throws Docx4JException,
	    JAXBException {
	List<Object> tables = getAllElementFromObject(
	        template.getMainDocumentPart(), Tbl.class);

	// 1. find the table
	Tbl tempTable = getTemplateTable(tables, placeholders[0]);
	CTBorder border = new CTBorder();
	border.setColor("red");
	border.setFrame(true);
	border.setSz(new BigInteger("14"));
	border.setSpace(new BigInteger("0"));
	border.setVal(STBorder.CIRCLES_LINES);

	TblBorders borders = new TblBorders();
	borders.setBottom(border);
	borders.setLeft(border);
	borders.setRight(border);
	borders.setTop(border);
	borders.setInsideH(border);
	borders.setInsideV(border);
	tempTable.getTblPr().setTblBorders(borders);
	List<Object> rows = getAllElementFromObject(tempTable, Tr.class);

	// first row is header, second row is content
	if (rows.size() == 2) {
	    // this is our template row
	    Tr templateRow = (Tr) rows.get(1);

	    for (Map<String, String> replacements : textToAdd) {
		// 2 and 3 are done in this method
		addRowToTable(tempTable, templateRow, replacements);
	    }

	    // 4. remove the template row
	    tempTable.getContent().remove(templateRow);
	}
    }

    public void replaceTablelist(String[] placeholders,
	    List<List<Map<String, String>>> textToAdd,

	    WordprocessingMLPackage template) throws Docx4JException,
	    JAXBException {
	List<Object> tables = getAllElementFromObject(
	        template.getMainDocumentPart(), Tbl.class);

	// 1. find the table
	Tbl tempTable = getTemplateTable(tables, placeholders[0]);
	CTBorder border = new CTBorder();
	border.setColor("red");
	border.setFrame(true);
	border.setSz(new BigInteger("14"));
	border.setSpace(new BigInteger("0"));
	border.setVal(STBorder.CIRCLES_LINES);

	List<Object> rows = getAllElementFromObject(tempTable, Tr.class);

	// first row is header, second row is content
	if (rows.size() == 2) {
	    // this is our template row
	    Tr templateRow = (Tr) rows.get(1);
	    for (List<Map<String, String>> r : textToAdd) {
		for (Map<String, String> replacements : r) {
		    // 2 and 3 are done in this method
		    addRowToTable(tempTable, templateRow, replacements);
		}
	    }
	    // 4. remove the template row
	    tempTable.getContent().remove(templateRow);
	}
    }

    public Tbl getTemplateTable(List<Object> tables, String templateKey)
	    throws Docx4JException, JAXBException {
	for (Iterator<Object> iterator = tables.iterator(); iterator.hasNext();) {
	    Object tbl = iterator.next();
	    List<?> textElements = getAllElementFromObject(tbl, Text.class);
	    for (Object text : textElements) {
		Text textElement = (Text) text;
		if (textElement.getValue() != null
		        && textElement.getValue().equals(templateKey))
		    return (Tbl) tbl;
	    }
	}
	return null;
    }

    public static void addRowToTable(Tbl reviewtable, Tr templateRow,
	    Map<String, String> replacements) {
	Tr workingRow = XmlUtils.deepCopy(templateRow);

	List textElements = getAllElementFromObject(workingRow, Text.class);
	for (Object object : textElements) {
	    Text text = (Text) object;
	    String replacementValue = replacements.get(text.getValue());
	    if (replacementValue != null)
		text.setValue(replacementValue);
	}

	reviewtable.getContent().add(workingRow);
    }

    public void addImageToPackage(WordprocessingMLPackage wordMLPackage,
	    byte[] bytes) throws Exception {
	BinaryPartAbstractImage imagePart = BinaryPartAbstractImage
	        .createImagePart(wordMLPackage, bytes);

	int docPrId = 1;
	int cNvPrId = 2;
	Inline inline = imagePart.createImageInline("Filename hint",
	        "Alternative text", docPrId, cNvPrId, false);

	P paragraph = addInlineImageToParagraph(inline);

	wordMLPackage.getMainDocumentPart().getContent().add(164, paragraph);

    }

    public P addInlineImageToParagraph(Inline inline) {
	// Now add the in-line image to a paragraph
	ObjectFactory factory = new ObjectFactory();
	P paragraph = factory.createP();
	R run = factory.createR();
	paragraph.getContent().add(run);
	Drawing drawing = factory.createDrawing();
	run.getContent().add(drawing);
	drawing.getAnchorOrInline().add(inline);
	return paragraph;
    }

    public byte[] convertImageToByteArray(File file)
	    throws FileNotFoundException, IOException {
	InputStream is = new FileInputStream(file);
	long length = file.length();
	// You cannot create an array using a long, it needs to be an int.
	if (length > Integer.MAX_VALUE) {
	    LOG.info("File too large!!");
	}
	byte[] bytes = new byte[(int) length];
	int offset = 0;
	int numRead = 0;
	while (offset < bytes.length
	        && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
	    offset += numRead;
	}
	// Ensure all the bytes have been read
	if (offset < bytes.length) {
	    LOG.info("Could not completely read file " + file.getName());
	}
	is.close();
	return bytes;
    }
}
