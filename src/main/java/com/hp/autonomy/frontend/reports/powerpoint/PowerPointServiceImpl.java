/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.reports.powerpoint;

import com.hp.autonomy.frontend.reports.powerpoint.dto.ComposableElement;
import com.hp.autonomy.frontend.reports.powerpoint.dto.DategraphData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ListData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.MapData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.ReportData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.SunburstData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TableData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TextData;
import com.hp.autonomy.frontend.reports.powerpoint.dto.TopicMapData;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFFreeformShape;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDoughnutChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStopList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import static com.hp.autonomy.frontend.reports.powerpoint.dto.ListData.Document;
import static com.hp.autonomy.frontend.reports.powerpoint.dto.MapData.Marker;
import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

public class PowerPointServiceImpl implements PowerPointService {

    private final TemplateSource pptxTemplate;

    public PowerPointServiceImpl(final TemplateSource pptxTemplate) {
        this.pptxTemplate = pptxTemplate;
    }

    public PowerPointServiceImpl() {
        this(TemplateSource.DEFAULT);
    }

    private SlideShowTemplate loadTemplate() throws SlideShowTemplate.LoadException {
        try(InputStream inputStream = pptxTemplate.getInputStream()) {
            return new SlideShowTemplate(inputStream);
        }
        catch(IOException e) {
            throw new SlideShowTemplate.LoadException("Error while loading template", e);
        }
    }

    @Override
    public XMLSlideShow topicmap(
            final TopicMapData data
    ) throws SlideShowTemplate.LoadException {
        final XMLSlideShow ppt = loadTemplate().getSlideShow();
        final Dimension pageSize = ppt.getPageSize();
        final XSLFSlide slide = ppt.createSlide();

        addTopicMap(slide, new Rectangle2D.Double(0, 0, pageSize.getWidth(), pageSize.getHeight()), data);

        return ppt;
    }

    private static void addTopicMap(final XSLFSlide slide, final Rectangle2D.Double anchor, final TopicMapData data) {
        for(final TopicMapData.Path reqPath : data.getPaths()) {
            final XSLFFreeformShape shape = slide.createFreeform();
            final Path2D.Double path = new Path2D.Double();

            boolean first = true;

            for(double[] point : reqPath.getPoints()) {
                final double x = point[0] * anchor.getWidth() + anchor.getMinX();
                final double y = point[1] * anchor.getHeight() + anchor.getMinY();
                if(first) {
                    path.moveTo(x, y);
                    first = false;
                }
                else {
                    path.lineTo(x, y);
                }
            }
            path.closePath();

            shape.setPath(path);
            shape.setStrokeStyle(2);
            shape.setLineColor(Color.GRAY);
            shape.setHorizontalCentered(true);
            shape.setVerticalAlignment(VerticalAlignment.MIDDLE);
            shape.setTextAutofit(TextShape.TextAutofit.NORMAL);

            final XSLFTextParagraph text = shape.addNewTextParagraph();
            final XSLFTextRun textRun = text.addNewTextRun();
            textRun.setText(reqPath.name);
            textRun.setFontColor(Color.WHITE);
            textRun.setBold(true);

            final int opacity = (int) (100000 * reqPath.getOpacity());
            final Color c1 = Color.decode(reqPath.getColor());
            final Color c2 = Color.decode(reqPath.getColor2());

            final CTShape cs = (CTShape) shape.getXmlObject();
            final CTGradientFillProperties gFill = cs.getSpPr().addNewGradFill();
            gFill.addNewLin().setAng(3300000);
            final CTGradientStopList list = gFill.addNewGsLst();

            final CTGradientStop stop1 = list.addNewGs();
            stop1.setPos(0);
            final CTSRgbColor color1 = stop1.addNewSrgbClr();
            color1.setVal(new byte[]{(byte) c1.getRed(), (byte) c1.getGreen(), (byte) c1.getBlue()});
            color1.addNewAlpha().setVal(opacity);

            final CTGradientStop stop2 = list.addNewGs();
            stop2.setPos(100000);
            final CTSRgbColor color2 = stop2.addNewSrgbClr();
            color2.setVal(new byte[]{(byte) c2.getRed(), (byte) c2.getGreen(), (byte) c2.getBlue()});
            color2.addNewAlpha().setVal(opacity);
        }
    }

    @Override
    public XMLSlideShow sunburst(
            final SunburstData data
    ) throws SlideShowTemplate.LoadException {
        if(!data.validateInput()) {
            throw new IllegalArgumentException("Number of values should match the number of categories");
        }

        final SlideShowTemplate template = loadTemplate();
        final XMLSlideShow ppt = template.getSlideShow();
        final XSLFSlide slide = ppt.createSlide();

        final int shapeId = 1;

        addSunburst(template, slide, null, data, shapeId, "relId" + shapeId);

        return ppt;
    }

    private static void addSunburst(final SlideShowTemplate template, final XSLFSlide slide, final Rectangle2D.Double anchor, final SunburstData data, final int shapeId, final String relId) throws SlideShowTemplate.LoadException {
        final String[] categories = data.getCategories();
        final double[] values = data.getValues();
        final String title = data.getTitle();

        slide.getXmlObject().getCSld().getSpTree().addNewGraphicFrame().set(template.getDoughnutChartShapeXML(relId, shapeId, "chart" + shapeId, anchor));

        final XSSFWorkbook workbook = new XSSFWorkbook();
        final XSSFSheet sheet = workbook.createSheet();

        final XSLFChart baseChart = template.getDoughnutChart();

        final CTChartSpace chartSpace = (CTChartSpace) baseChart.getCTChartSpace().copy();
        final CTChart ctChart = chartSpace.getChart();
        final CTPlotArea plotArea = ctChart.getPlotArea();

        if (StringUtils.isEmpty(title)) {
            if (ctChart.getAutoTitleDeleted() != null) {
                ctChart.getAutoTitleDeleted().setVal(true);
            }

            ctChart.unsetTitle();
        }

        final CTDoughnutChart donutChart = plotArea.getDoughnutChartArray(0);

        final CTPieSer series = donutChart.getSerArray(0);

        final CTStrRef strRef = series.getTx().getStrRef();
        strRef.getStrCache().getPtArray(0).setV(title);
        sheet.createRow(0).createCell(1).setCellValue(title);
        strRef.setF(new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString());

        final CTStrRef categoryRef = series.getCat().getStrRef();
        final CTStrData categoryData = categoryRef.getStrCache();
        final CTNumRef numRef = series.getVal().getNumRef();
        final CTNumData numericData = numRef.getNumCache();

        categoryData.setPtArray(null);
        numericData.setPtArray(null);

        for(int idx = 0; idx < values.length; ++idx) {
            final CTStrVal categoryPoint = categoryData.addNewPt();
            categoryPoint.setIdx(idx);
            categoryPoint.setV(categories[idx]);

            final CTNumVal numericPoint = numericData.addNewPt();
            numericPoint.setIdx(idx);
            numericPoint.setV(Double.toString(values[idx]));

            XSSFRow row = sheet.createRow(idx + 1);
            row.createCell(0).setCellValue(categories[idx]);
            row.createCell(1).setCellValue(values[idx]);
        }
        categoryData.getPtCount().setVal(categories.length);
        numericData.getPtCount().setVal(values.length);

        categoryRef.setF(new CellRangeAddress(1, values.length, 0, 0).formatAsString(sheet.getSheetName(), true));
        numRef.setF(new CellRangeAddress(1, values.length, 1, 1).formatAsString(sheet.getSheetName(), true));

        try {
            writeChart(template.getSlideShow(), slide, baseChart, chartSpace, workbook, relId);
        }
        catch(IOException|InvalidFormatException e) {
            throw new SlideShowTemplate.LoadException("Error writing chart in loaded template", e);
        }
    }

    @Override
    public XMLSlideShow table(
            final String title,
            final TableData tableData

    ) throws SlideShowTemplate.LoadException {
        final int rows = tableData.getRows(),
                  cols = tableData.getCols();
        final String[] data = tableData.getCells();

        final XMLSlideShow ppt = loadTemplate().getSlideShow();
        final Dimension pageSize = ppt.getPageSize();
        final double pageWidth = pageSize.getWidth(), pageHeight = pageSize.getHeight();
        final XSLFSlide sl = ppt.createSlide();

        final XSLFTextBox textBox = sl.createTextBox();
        textBox.setText(title);
        textBox.setHorizontalCentered(true);
        textBox.setTextAutofit(TextShape.TextAutofit.SHAPE);
        final Rectangle2D.Double textBounds = new Rectangle2D.Double(0, 0.05 * pageHeight, pageWidth, 0.1 * pageHeight);
        textBox.setAnchor(textBounds);

        addTable(sl, new Rectangle2D.Double(0, textBounds.getMaxY(), pageWidth, pageHeight), rows, cols, data, false);

        return ppt;
    }

    private static void addTable(final XSLFSlide slide, final Rectangle2D.Double anchor, final int rows, final int cols, final String[] data, final boolean crop) {
        final XSLFTable table = slide.createTable(rows, cols);

        int idx = 0;

        double availWidth = anchor.getWidth();
        double tableW = 0;

        if (cols == 2) {
            // In the most common situation, there's a count column which should be relatively smaller.
            // Make it take 10%, or 70 pixels, whichever is bigger, unless that's more than 50% of the overall space.
            final double minCountWidth = 70;
            final double countColWidth = Math.min(0.5 * availWidth, Math.max(minCountWidth, availWidth * 0.1));
            table.setColumnWidth(0, availWidth - countColWidth);
            table.setColumnWidth(1, countColWidth);
            tableW += table.getColumnWidth(0);
            tableW += table.getColumnWidth(1);
        }
        else {
            for(int col = 0; col < cols; ++col) {
                table.setColumnWidth(col, availWidth / cols);
                tableW += table.getColumnWidth(col);
            }
        }

        // PowerPoint won't auto-shrink the table for you; and the POI API can't calculate the heights, so we just
        //   have to assume the total row heights add up to be the table height.
        double tableH = 0;

        for(int row = 0; row < rows; ++row) {
            for(int col = 0; col < cols; ++col) {
                final XSLFTableCell cell = table.getCell(row, col);
                cell.setText(data[idx++]);

                for(final TableCell.BorderEdge edge : TableCell.BorderEdge.values()) {
                    cell.setBorderColor(edge, Color.BLACK);
                }
            }

            final double nextH = tableH + table.getRowHeight(row);

            if (crop && nextH > anchor.getHeight() && row < rows - 1) {
                table.mergeCells(row, row, 0, cols - 1);
                // ellipsis
                table.getCell(row, 0).setText("\u2026");
                break;
            }
            else {
                tableH = nextH;
            }
        }

        final double width = Math.min(tableW, availWidth);

        table.setAnchor(new Rectangle2D.Double(anchor.getMinX() + 0.5 * (availWidth - width), anchor.getMinY(), width, Math.min(tableH, anchor.getHeight())));
    }

    @Override
    public XMLSlideShow map(
            final String title,
            final MapData map
    ) throws SlideShowTemplate.LoadException {
        final String image = map.getImage();

        final XMLSlideShow ppt = loadTemplate().getSlideShow();
        final Dimension pageSize = ppt.getPageSize();
        final double pageWidth = pageSize.getWidth(), pageHeight = pageSize.getHeight();
        final XSLFSlide sl = ppt.createSlide();

        final XSLFTextBox textBox = sl.createTextBox();
        textBox.clearText();
        final XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
        paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        paragraph.addNewTextRun().setText(title);
        textBox.setHorizontalCentered(true);
        textBox.setTextAutofit(TextShape.TextAutofit.SHAPE);
        final Rectangle2D.Double textBounds = new Rectangle2D.Double(0, 0.05 * pageHeight, pageWidth, 0.1 * pageHeight);
        textBox.setAnchor(textBounds);

        final XSLFPictureData picture = addPictureData(ppt, image);
        final double offsetY = textBounds.getMaxY();
        addMap(sl, new Rectangle2D.Double(0, offsetY, pageWidth, pageHeight - textBounds.getMaxY()), picture, map.getMarkers());

        return ppt;
    }

    private static XSLFPictureData addPictureData(final XMLSlideShow ppt, final String image) {
        final PictureData.PictureType type;
        if(image.startsWith("data:image/png;base64,")) {
            type = PictureData.PictureType.PNG;
        }
        else if(image.startsWith("data:image/jpeg;base64,")) {
            type = PictureData.PictureType.JPEG;
        }
        else {
            throw new IllegalArgumentException("Unsupported image type");
        }

        final byte[] bytes = Base64.decodeBase64(image.split(",")[1]);
        return ppt.addPicture(bytes, type);
    }

    private static XSLFPictureShape addMap(final XSLFSlide slide, final Rectangle2D.Double anchor, final XSLFPictureData picture, final Marker[] markers) {
        double tgtW = anchor.getWidth(),
               tgtH = anchor.getHeight();

        final Dimension size = picture.getImageDimension();
        final double ratio = size.getWidth() / size.getHeight();

        if(ratio > tgtW / tgtH) {
            // source image is wider than target, clip fixed width variable height
            tgtH = tgtW / ratio;
        }
        else {
            tgtW = tgtH * ratio;
        }

        final XSLFPictureShape canvas = slide.createPicture(picture);
        // Vertically align top, horizontally-align center
        final double offsetX = anchor.getMinX() + 0.5 * (anchor.getWidth() - tgtW),
                     offsetY = anchor.getMinY();
        canvas.setAnchor(new Rectangle2D.Double(offsetX, offsetY, tgtW, tgtH));

        for(Marker marker : markers) {
            final Color color = Color.decode(marker.getColor());
            final double centerX = offsetX + marker.getX() * tgtW;
            final double centerY = offsetY + marker.getY() * tgtH;

            if(marker.isCluster()) {
                final XSLFGroupShape group = slide.createGroup();
                double halfMark = 10;
                double mark = halfMark * 2;
                double innerHalfMark = 7;
                double innerMark = innerHalfMark * 2;
                // align these so the middle is the latlng position
                final Rectangle2D.Double groupAnchor = new Rectangle2D.Double(centerX - halfMark, centerY - halfMark, mark, mark);

                group.setAnchor(groupAnchor);
                group.setInteriorAnchor(groupAnchor);

                final XSLFAutoShape shape = group.createAutoShape();
                shape.setShapeType(ShapeType.ELLIPSE);
                final boolean fade = marker.isFade();
                // There's a 0.3 alpha transparency (255 * 0.3 is 76) when a marker is faded out
                final int FADE_ALPHA = 76;
                shape.setFillColor(transparentColor(color, fade ? 47 : 154));
                shape.setAnchor(groupAnchor);

                final XSLFAutoShape inner = group.createAutoShape();
                inner.setFillColor(fade ? transparentColor(color, FADE_ALPHA) : color);
                inner.setLineWidth(0.1);
                inner.setLineColor(new Color((int) (color.getRed() * 0.9), (int) (color.getGreen() * 0.9), (int) (color.getBlue() * 0.9), fade ? FADE_ALPHA : 255));
                inner.setShapeType(ShapeType.ELLIPSE);
                inner.setHorizontalCentered(true);
                inner.setWordWrap(false);
                inner.setVerticalAlignment(VerticalAlignment.MIDDLE);
                inner.clearText();
                final XSLFTextParagraph para = inner.addNewTextParagraph();
                para.setTextAlign(TextParagraph.TextAlign.CENTER);
                final XSLFTextRun text = para.addNewTextRun();
                text.setFontSize(6.0);
                final Color fontColor = Color.decode(StringUtils.defaultString(marker.getFontColor(), "#000000"));
                text.setFontColor(fade ? transparentColor(fontColor, FADE_ALPHA) : fontColor);
                text.setText(marker.getText());
                inner.setAnchor(new Rectangle2D.Double(centerX - innerHalfMark, centerY - innerHalfMark, innerMark, innerMark));
            }
            else {
                final XSLFAutoShape shape = slide.createAutoShape();
                shape.setHorizontalCentered(true);
                shape.setWordWrap(false);
                shape.setShapeType(ShapeType.TEARDROP);
                shape.setVerticalAlignment(VerticalAlignment.BOTTOM);
                shape.setRotation(135);
                shape.setLineWidth(1.0);
                shape.setLineColor(color.darker());
                shape.setFillColor(transparentColor(color, 210));
                double halfMark = 8;
                double mark = halfMark * 2;
                // align these so the pointy end at the bottom is the latlng position
                shape.setAnchor(new Rectangle2D.Double(centerX - halfMark, centerY - mark, mark, mark));

                // We create a hyperlink which links back to this slide; so we get hover-over-detail-text on the marker
                final CTHyperlink link = ((CTShape) shape.getXmlObject()).getNvSpPr().getCNvPr().addNewHlinkClick();
                link.setTooltip(marker.getText());
                final PackageRelationship rel = shape.getSheet().getPackagePart().addRelationship(slide.getPackagePart().getPartName(),
                        TargetMode.INTERNAL, XSLFRelation.SLIDE.getRelation());
                link.setId(rel.getId());
                link.setAction("ppaction://hlinksldjump");
            }
        }

        return canvas;
    }

    private static Color transparentColor(final Color color, final int a) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
    }

    @Override
    public XMLSlideShow list(
            final String results,
            final String sortBy,
            final ListData documentList
    ) throws SlideShowTemplate.LoadException {
        final Document[] docs = documentList.getDocs();

        final XMLSlideShow ppt = loadTemplate().getSlideShow();
        final Dimension pageSize = ppt.getPageSize();
        addList(ppt, null, new Rectangle2D.Double(0, 0, pageSize.getWidth(), pageSize.getHeight()), true, docs, results, sortBy);

        return ppt;
    }

    private static void addList(final XMLSlideShow ppt, XSLFSlide sl, final Rectangle2D.Double anchor, final boolean paginate, final Document[] docs, final String results, final String sortBy) {
        final double
                // How much space to leave at the left and right edge of the slide
                xMargin = 20,
                // How much space to leave at the top
                yMargin = 5,
                // Size of the icon
                iconWidth = 20, iconHeight = 24,
                // Find's thumbnail height is 97px by 55px, hardcoded in the CSS in .document-thumbnail
                thumbScale = 0.8,
                thumbW = 97 * thumbScale, thumbH = 55 * thumbScale,
                // Margin around the thumbnail
                thumbMargin = 4.,
                // Space between list items
                listItemMargin = 5.;

        final Pattern highlightPattern = Pattern.compile("<HavenSearch-QueryText-Placeholder>(.*?)</HavenSearch-QueryText-Placeholder>");

        double yCursor = yMargin + anchor.getMinY(), xCursor = xMargin + anchor.getMinX();

        int docsOnPage = 0;

        for(int docIdx = 0; docIdx < docs.length; ++docIdx) {
            final Document doc = docs[docIdx];

            if(sl == null) {
                sl = ppt.createSlide();
                yCursor = yMargin + anchor.getMinY();
                xCursor = xMargin + anchor.getMinX();
                docsOnPage = 0;

                double yStep = 0;

                if (StringUtils.isNotBlank(results)) {
                    final XSLFTextBox textBox = sl.createTextBox();
                    textBox.clearText();
                    final Rectangle2D.Double textBounds = new Rectangle2D.Double(xCursor, yCursor, Math.max(0, anchor.getMaxX() - xCursor - xMargin), 20);
                    textBox.setAnchor(textBounds);

                    addTextRun(textBox.addNewTextParagraph(), results, 12., Color.LIGHT_GRAY);

                    yStep = textBox.getTextHeight();
                }

                if (StringUtils.isNotBlank(sortBy)) {
                    final XSLFTextBox sortByEl = sl.createTextBox();
                    sortByEl.clearText();
                    final XSLFTextParagraph sortByText = sortByEl.addNewTextParagraph();
                    sortByText.setTextAlign(TextParagraph.TextAlign.RIGHT);

                    addTextRun(sortByText, sortBy, 12., Color.LIGHT_GRAY);

                    sortByEl.setAnchor(new Rectangle2D.Double(xCursor, yCursor, Math.max(0, anchor.getMaxX() - xCursor - xMargin), 20));

                    yStep = Math.max(sortByEl.getTextHeight(), yStep);
                }

                if (yStep > 0) {
                    yCursor += listItemMargin + yStep;
                }
            }

            final XSLFAutoShape icon = sl.createAutoShape();
            icon.setShapeType(ShapeType.SNIP_1_RECT);
            icon.setAnchor(new Rectangle2D.Double(xCursor, yCursor + listItemMargin, iconWidth, iconHeight));
            icon.setLineColor(Color.decode("#888888"));
            icon.setLineWidth(2.0);

            xCursor += iconWidth;

            final XSLFTextBox listEl = sl.createTextBox();
            listEl.clearText();
            listEl.setAnchor(new Rectangle2D.Double(xCursor, yCursor, Math.max(0, anchor.getMaxX() - xCursor - xMargin), Math.max(0, anchor.getMaxY() - yCursor)));

            final XSLFTextParagraph titlePara = listEl.addNewTextParagraph();
            addTextRun(titlePara, doc.getTitle(), 14.0, Color.BLACK).setBold(true);

            if (StringUtils.isNotBlank(doc.getDate())) {
                final XSLFTextParagraph datePara = listEl.addNewTextParagraph();
                datePara.setLeftMargin(5.);
                addTextRun(datePara, doc.getDate(), 10., Color.GRAY).setItalic(true);
            }

            if (StringUtils.isNotBlank(doc.getRef())) {
                addTextRun(listEl.addNewTextParagraph(), doc.getRef(), 12., Color.GRAY);
            }

            final double thumbnailOffset = listEl.getTextHeight();

            final XSLFTextParagraph contentPara = listEl.addNewTextParagraph();

            XSLFPictureShape picture = null;

            if (StringUtils.isNotBlank(doc.getThumbnail())) {
                try {
                    final byte[] imageData = Base64.decodeBase64(doc.getThumbnail());
                    // Picture reuse is automatic
                    picture = sl.createPicture(ppt.addPicture(imageData, PictureData.PictureType.JPEG));
                    picture.setAnchor(new Rectangle2D.Double(xCursor, yCursor + thumbnailOffset + thumbMargin, thumbW, thumbH));

                    // If there is enough horizontal space, put the text summary to the right of the thumbnail image,
                    //    otherwise put it under the thumbnail,
                    if (listEl.getAnchor().getWidth() > 2.5 * thumbW) {
                        contentPara.setLeftMargin(thumbW);
                    }
                    else {
                        contentPara.addLineBreak().setFontSize(thumbH);
                    }

                }
                catch(RuntimeException e) {
                    // if there's any errors, we'll just ignore the image
                }
            }

            final String rawSummary = doc.getSummary();
            if (StringUtils.isNotBlank(rawSummary)) {
                // HTML treats newlines and multiple whitespace as a single whitespace.
                final String summary = rawSummary.replaceAll("\\s+", " ");
                final Matcher matcher = highlightPattern.matcher(summary);
                int idx = 0;

                while(matcher.find()) {
                    final int start = matcher.start();

                    if (idx < start) {
                        addTextRun(contentPara, summary.substring(idx, start), 12., Color.DARK_GRAY);
                    }

                    addTextRun(contentPara, matcher.group(1), 12., Color.DARK_GRAY).setBold(true);
                    idx = matcher.end();
                }

                if (idx < summary.length()) {
                    addTextRun(contentPara, summary.substring(idx), 12., Color.DARK_GRAY);
                }
            }

            double elHeight = Math.max(listEl.getTextHeight(), iconHeight);
            if (picture != null) {
                elHeight = Math.max(elHeight, picture.getAnchor().getMaxY() - yCursor);
            }

            yCursor += elHeight;
            xCursor = xMargin + anchor.getMinX();

            docsOnPage++;

            if (yCursor > anchor.getMaxY()) {
                if (docsOnPage > 1) {
                    // If we drew more than one list element on this page; and we exceeded the available space,
                    //   delete the last element's shapes and redraw it on the next page.
                    sl.removeShape(listEl);
                    sl.removeShape(icon);

                    if (picture != null) {
                        // Technically we want to remove the shape, but that also removes the related image data,
                        //   which will be shared with other images; causing problems when trying to render them.
                        // Workaround is to just hide the image out of view.
                        picture.setAnchor(new Rectangle2D.Double(-1, -1, 0.1, 0.1));
                    }
                    --docIdx;
                }

                sl = null;

                if (!paginate) {
                    break;
                }
            }
            else {
                yCursor += listItemMargin;
            }
        }
    }

    private static XSLFTextRun addTextRun(final XSLFTextParagraph paragraph, final String text, final double fontSize, final Color color) {
        final XSLFTextRun summary = paragraph.addNewTextRun();
        summary.setFontColor(color);
        summary.setText(text);
        summary.setFontSize(fontSize);
        return summary;
    }

    @Override
    public XMLSlideShow graph(
            final DategraphData data
    ) throws SlideShowTemplate.LoadException {
        final SlideShowTemplate template = loadTemplate();
        final XMLSlideShow ppt = template.getSlideShow();
        final int shapeId = 1;
        final String relId = "relId" + shapeId;

        addDategraph(template, ppt.createSlide(), null, data, shapeId, relId);

        return ppt;
    }

    private static void addDategraph(final SlideShowTemplate template, final XSLFSlide slide, final Rectangle2D.Double anchor, final DategraphData data, final int shapeId, final String relId) throws SlideShowTemplate.LoadException {
        if (!data.validateInput()) {
            throw new IllegalArgumentException("Invalid data provided");
        }

        final List<DategraphData.Row> rows = data.getRows();
        boolean useSecondaryAxis = rows.stream().anyMatch(DategraphData.Row::isSecondaryAxis);

        if (rows.stream().allMatch(DategraphData.Row::isSecondaryAxis)) {
            // If everything is on the secondary axis; just use the primary axis
            rows.forEach(row -> row.setSecondaryAxis(false));
            useSecondaryAxis = false;
        }

        final XSSFWorkbook wb = writeChart(data);

        final XMLSlideShow ppt = template.getSlideShow();

        slide.getXmlObject().getCSld().getSpTree().addNewGraphicFrame().set(template.getGraphChartShapeXML(relId, shapeId, "chart" + shapeId, anchor));

        XSLFChart baseChart = template.getGraphChart();
        final CTChartSpace chartSpace = (CTChartSpace) baseChart.getCTChartSpace().copy();

        final CTChart ctChart = chartSpace.getChart();
        final CTPlotArea plotArea = ctChart.getPlotArea();
        final XSSFSheet sheet = wb.getSheetAt(0);

        // In the template, we have two <c:lineChart> objects, one for the primary axis, one for the secondary.
        if (!useSecondaryAxis) {
            // Discard the extra axes
            // OpenOffice is happy enough if you remove the line chart, but PowerPoint will complain it's a corrupt
            //   file and unhelpfully delete the entire chart when you choose 'repair' if any orphan axes remain.
            plotArea.removeLineChart(1);
            plotArea.removeValAx(1);
            plotArea.removeDateAx(1);
        }

        final CTLineChart primaryChart = plotArea.getLineChartArray()[0];
        final CTLineSer[] primarySeries = primaryChart.getSerArray();
        primarySeries[0].getDPtList().clear();

        int primarySeriesCount = 0;
        int secondarySeriesCount = 0;

        for (int seriesIdx = 0; seriesIdx < rows.size(); ++seriesIdx) {
            final DategraphData.Row row = rows.get(seriesIdx);

            final CTLineChart tgtChart = plotArea.getLineChartArray(row.isSecondaryAxis() ? 1 : 0);

            final CTLineSer[] serArray = tgtChart.getSerArray();
            final int createdSeriesIdx = row.isSecondaryAxis() ? secondarySeriesCount++ : primarySeriesCount++;

            final CTLineSer curSeries;

            if (createdSeriesIdx < serArray.length) {
                curSeries = serArray[createdSeriesIdx];
            }
            else {
                curSeries = tgtChart.addNewSer();
                curSeries.set(serArray[0].copy());
            }

            updateCTLineSer(data, sheet, seriesIdx, curSeries);
        }

        try {
            writeChart(ppt, slide, baseChart, chartSpace, wb, relId);
        }
        catch(IOException|InvalidFormatException e) {
            throw new SlideShowTemplate.LoadException("Unexpected error writing files from loaded template", e);
        }
    }

    private static void updateCTLineSer(final DategraphData data, final XSSFSheet sheet, final int seriesIdx, final CTLineSer series) {
        final String sheetName = sheet.getSheetName();

        // the series idx starts from 0
        final DategraphData.Row row = data.getRows().get(seriesIdx);
        final String title = row.getLabel();
        final Color color = Color.decode(row.getColor());

        series.getOrder().setVal(seriesIdx);
        series.getIdx().setVal(seriesIdx);

        final CTSolidColorFillProperties fill = series.getSpPr().getLn().getSolidFill();

        // We have to set any possible colour type, PowerPoint throws an error if there's multiple fills, and we don't
        //   know what colour type the user may have used in their template slide.
        if (fill.getSchemeClr() != null) {
            fill.unsetSchemeClr();
        }
        if (fill.getSrgbClr() != null) {
            fill.unsetSrgbClr();
        }
        if (fill.getHslClr() != null) {
            fill.unsetHslClr();
        }
        if (fill.getPrstClr() != null) {
            fill.unsetPrstClr();
        }
        if (fill.getScrgbClr() != null) {
            fill.unsetScrgbClr();
        }
        if (fill.getSysClr() != null) {
            fill.unsetSysClr();
        }

        final CTSRgbColor fillClr = fill.addNewSrgbClr();
        fillClr.setVal(new byte[]{ (byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()});

        final CTStrRef strRef = series.getTx().getStrRef();
        strRef.getStrCache().getPtArray()[0].setV(title);

        strRef.setF(new CellReference(sheetName, 0, seriesIdx + 1, true, true).formatAsString());

        final long[] timestamps = data.getTimestamps();
        {
            final CTNumRef timestampCatNumRef = series.getCat().getNumRef();
            timestampCatNumRef.setF(new AreaReference(
                new CellReference(sheetName, 1, 0, true, true),
                new CellReference(sheetName, 1 + timestamps.length, 0, true, true)
            ).formatAsString());

            final CTNumData timeStampCatNumCache = timestampCatNumRef.getNumCache();
            timeStampCatNumCache.getPtCount().setVal(timestamps.length);
            timeStampCatNumCache.setPtArray(null);

            for(int ii = 0; ii < timestamps.length; ++ii) {
                final CTNumVal pt = timeStampCatNumCache.addNewPt();
                pt.setIdx(ii);
                pt.setV(sheet.getRow(1 + ii).getCell(0).getRawValue());
            }
        }

        {
            final double[] seriesData = row.getValues();

            final CTNumRef valuesNumRef = series.getVal().getNumRef();
            valuesNumRef.setF(new AreaReference(
                new CellReference(sheetName, 1, seriesIdx + 1, true, true),
                new CellReference(sheetName, 1 + timestamps.length, seriesIdx + 1, true, true)
            ).formatAsString());

            final CTNumData valuesNumCache = valuesNumRef.getNumCache();
            valuesNumCache.getPtCount().setVal(timestamps.length);
            valuesNumCache.setPtArray(null);

            for(int ii = 0; ii < timestamps.length; ++ii) {
                final CTNumVal pt = valuesNumCache.addNewPt();
                pt.setIdx(ii);
                pt.setV(Double.toString(seriesData[ii]));
            }
        }
    }

    private static XSSFWorkbook writeChart(final DategraphData data) {
        final XSSFWorkbook wb = new XSSFWorkbook();
        final XSSFSheet sheet = wb.createSheet("Sheet1");

        final CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat((short) 14);

        final List<DategraphData.Row> rows = data.getRows();
        final long[] timestamps = data.getTimestamps();

        final XSSFRow header = sheet.createRow(0);
        header.createCell(0).setCellValue("Timestamp");
        for (int ii = 0; ii < rows.size(); ++ii) {
            header.createCell(ii + 1).setCellValue(rows.get(ii).getLabel());
        }

        for (int rowIdx = 0; rowIdx < timestamps.length; ++rowIdx) {
            final XSSFRow row = sheet.createRow(rowIdx + 1);

            final XSSFCell cell = row.createCell(0);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(new Date(timestamps[rowIdx] * 1000));

            for (int ii = 0; ii < rows.size(); ++ii) {
                row.createCell(ii + 1).setCellValue(rows.get(ii).getValues()[rowIdx]);
            }
        }

        return wb;
    }

    @Override
    public XMLSlideShow report(
            final ReportData report
    ) throws SlideShowTemplate.LoadException {
        final SlideShowTemplate template = loadTemplate();
        final XMLSlideShow ppt = template.getSlideShow();
        final Dimension pageSize = ppt.getPageSize();
        double width = pageSize.getWidth();
        double height = pageSize.getHeight();

        final XSLFSlide slide = ppt.createSlide();

        // We need to add charts first, since calling slide.getShapes() or indirectly createShape() etc.
        //   before adding chart objects directly via XML will break things.
        Arrays.sort(report.getChildren(), Comparator.comparingInt(PowerPointServiceImpl::prioritizeCharts));

        // For the same reason, we need to have a separate slide to place our sizing textbox for calculations.
        final XSLFSlide sizingSlide = ppt.createSlide();
        int shapeId = 1;

        for(final ReportData.Child child : report.getChildren()) {
            final ComposableElement data = child.getData();
            final Rectangle2D.Double anchor = new Rectangle2D.Double(width * child.getX(), height * child.getY(), width * child.getWidth(), height * child.getHeight());

            if (child.getMargin() >= 0) {
                final double margin = child.getMargin();
                final double marginX2 = margin * 2;
                final double textMargin = child.getTextMargin();

                if (anchor.getWidth() > marginX2) {
                    double xCursor = anchor.getMinX() + margin,
                           xWidthAvail = anchor.getWidth() - marginX2,
                           yCursor = anchor.getMinY() + margin,
                           yHeightAvail = anchor.getHeight() - marginX2;
                    XSLFTextBox sizingBox = null;

                    final String title = child.getTitle();
                    if (StringUtils.isNotEmpty(title) && yHeightAvail > 0) {
                        sizingBox = sizingSlide.createTextBox();
                        final Rectangle2D.Double sizingAnchor = new Rectangle2D.Double(
                                xCursor,
                                yCursor,
                                xWidthAvail,
                                yHeightAvail);
                        sizingBox.setAnchor(sizingAnchor);
                        sizingBox.clearText();
                        addTextRun(sizingBox.addNewTextParagraph(), title, child.getFontSize(), Color.BLACK).setFontFamily(child.getFontFamily());

                        final double textHeight = sizingBox.getTextHeight() + textMargin;
                        yCursor += textHeight;
                        yHeightAvail -= textHeight;
                    }

                    if (yHeightAvail > 0) {
                        anchor.setRect(xCursor, yCursor, xWidthAvail, yHeightAvail);
                    }
                    else if (sizingBox != null) {
                        sizingSlide.removeShape(sizingBox);
                    }
                }
            }

            if (data instanceof DategraphData) {
                addDategraph(template, slide, anchor, (DategraphData) data, shapeId, "relId" + shapeId);
                shapeId++;
            }
            else if (data instanceof ListData) {
                final ListData listData = (ListData) data;
                addList(ppt, slide, anchor, false, listData.getDocs(), null, null);
            }
            else if (data instanceof MapData) {
                final MapData mapData = (MapData) data;
                addMap(slide, anchor, addPictureData(ppt, mapData.getImage()), mapData.getMarkers());
            }
            else if (data instanceof SunburstData) {
                addSunburst(template, slide, anchor, (SunburstData) data, shapeId, "relId" + shapeId);
                shapeId++;
            }
            else if (data instanceof TableData) {
                final TableData tableData = (TableData) data;
                addTable(slide, anchor, tableData.getRows(), tableData.getCols(), tableData.getCells(), true);
            }
            else if (data instanceof TopicMapData) {
                addTopicMap(slide, anchor, (TopicMapData) data);
            }
            else if (data instanceof TextData) {
                addTextData(slide, anchor, (TextData) data);
            }
        }

        // Clone all text boxes to the original slide afterward, and remove the sizing slide
        for(XSLFShape shape : sizingSlide.getShapes()) {
            if (shape instanceof XSLFTextBox) {
                final XSLFTextBox src = (XSLFTextBox) shape;
                final XSLFTextBox textBox = slide.createTextBox();
                textBox.setAnchor(src.getAnchor());
                textBox.clearText();
                src.forEach(srcPara -> textBox.addNewTextParagraph().getXmlObject().set(srcPara.getXmlObject().copy()));
            }
        }
        ppt.removeSlide(1);

        return ppt;
    }

    private void addTextData(final XSLFSlide slide, final Rectangle2D.Double anchor, final TextData data) {
        final XSLFTextBox textBox = slide.createTextBox();
        textBox.setAnchor(anchor);
        textBox.clearText();

        final XSLFTextParagraph para = textBox.addNewTextParagraph();

        for(final TextData.Paragraph runData : data.getText()) {
            final XSLFTextRun run = para.addNewTextRun();
            run.setText(runData.getText());
            run.setFontSize(runData.getFontSize());
            run.setBold(runData.isBold());
            run.setItalic(runData.isItalic());
            run.setFontColor(Color.decode(runData.getColor()));

            if (textBox.getTextHeight() > anchor.getHeight()) {
                // Try removing words from the last box until we find something that fits, or we run out of words
                final String trimmedText = runData.getText().trim();
                run.setText(trimmedText);

                for (final StringBuilder text = new StringBuilder(trimmedText); textBox.getTextHeight() > anchor.getHeight() && text.length() > 0 ; ) {
                    final int lastSpaceIdx = Math.max(text.lastIndexOf(" "), text.lastIndexOf("\n"));

                    if (lastSpaceIdx < 0) {
                        break;
                    }

                    text.delete(lastSpaceIdx, text.length());
                    // Add a trailing ellipsis unless it's empty or already contained a trailing ellipsis or "..." at the final truncated position.
                    run.setText(text.length() > 0 ? text.toString().replaceFirst("(\\s*(\\.{3}|\u2026))?$", "\u2026") : "");
                }

                // The font metrics aren't going to be perfect (due to unavailability of fonts etc.) so we force the truncated text to fit.
                textBox.setTextAutofit(TextShape.TextAutofit.NORMAL);

                break;
            }
        }
    }

    private static int prioritizeCharts(final ReportData.Child child) {
        final ComposableElement d = child.getData();
        return d instanceof DategraphData || d instanceof SunburstData ? -1 : 0;
    }

    private static PackagePartName generateNewName(final OPCPackage opcPackage, final String baseName) throws InvalidFormatException {
        final Pattern pattern = Pattern.compile("(.*?)(\\d+)(\\.\\w+)?$");

        final Matcher matcher = pattern.matcher(baseName);

        if (matcher.find()) {
            int num = Integer.parseInt(matcher.group(2));

            for (int ii = num + 1; ii < Integer.MAX_VALUE; ++ii) {
                final PackagePartName testName = PackagingURIHelper.createPartName(matcher.group(1) + ii + matcher.group(3));

                if (opcPackage.getPart(testName) == null) {
                    return testName;
                }
            }
        }

        // If the document doesn't have a numeric extension, just return it
        return PackagingURIHelper.createPartName(baseName);
    }

    private static void writeChart(final XMLSlideShow pptx, final XSLFSlide slide, final XSLFChart templateChart, final CTChartSpace modifiedChart, final XSSFWorkbook workbook, final String relId) throws IOException, InvalidFormatException {
        final OPCPackage opcPackage = pptx.getPackage();
        final PackagePartName chartName = generateNewName(opcPackage, templateChart.getPackagePart().getPartName().getURI().getPath());

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));
        modifiedChart.save(baos, xmlOptions);

        final PackagePart chartPart = opcPackage.createPart(chartName, XSLFRelation.CHART.getContentType(), baos);

        slide.getPackagePart().addRelationship(chartName, TargetMode.INTERNAL, XSLFRelation.CHART.getRelation(), relId);

        for(final POIXMLDocumentPart.RelationPart part : templateChart.getRelationParts()) {
            final ByteArrayOutputStream partCopy = new ByteArrayOutputStream();
            final URI targetURI = part.getRelationship().getTargetURI();

            final PackagePartName name = generateNewName(opcPackage, targetURI.getPath());

            final String contentType = part.getDocumentPart().getPackagePart().getContentType();

            if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
                workbook.write(partCopy);
            }
            else {
                IOUtils.copy(part.getDocumentPart().getPackagePart().getInputStream(), partCopy);
            }

            opcPackage.createPart(name, contentType, partCopy);
            chartPart.addRelationship(name, TargetMode.INTERNAL, part.getRelationship().getRelationshipType());
        }
    }
}