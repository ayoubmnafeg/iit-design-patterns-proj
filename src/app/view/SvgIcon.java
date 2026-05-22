package app.view;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import javafx.scene.image.ImageView;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;

public class SvgIcon {

    public static ImageView render(String resourcePath, int size) {
        return renderInternal(resourcePath, size, 0, 0f);
    }

    public static ImageView renderTinted(String resourcePath, int size, int tintArgb) {
        float alpha = ((tintArgb >> 24) & 0xFF) / 255f;
        return renderInternal(resourcePath, size, tintArgb, alpha);
    }

    public static ImageView renderRecolored(String resourcePath, int size, int rgb) {
        ImageView iv = render(resourcePath, size);
        if (iv.getImage() == null) return iv;

        BufferedImage bi = javafx.embed.swing.SwingFXUtils.fromFXImage(iv.getImage(), null);
        int color = rgb & 0x00FFFFFF;
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int argb = bi.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) continue;

                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;
                if (r > 235 && g > 235 && b > 235) continue;

                bi.setRGB(x, y, (alpha << 24) | color);
            }
        }

        iv.setImage(javafx.embed.swing.SwingFXUtils.toFXImage(bi, null));
        return iv;
    }

    private static ImageView renderInternal(String resourcePath, int size, int tintArgb, float tintAlpha) {
        ImageView iv = new ImageView();
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setSmooth(true);
        iv.setPreserveRatio(true);
        try {
            URL url = SvgIcon.class.getResource(resourcePath);
            if (url == null) return iv;

            SVGUniverse universe = SVGCache.getSVGUniverse();
            URI uri = universe.loadSVG(url);
            SVGDiagram diagram = universe.getDiagram(uri);
            diagram.setIgnoringClipHeuristic(true);

            BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            float svgW = diagram.getWidth();
            float svgH = diagram.getHeight();
            if (svgW > 0 && svgH > 0) g2.scale(size / svgW, size / svgH);
            diagram.render(g2);

            if (tintAlpha > 0f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, tintAlpha));
                g2.setColor(new java.awt.Color((tintArgb >> 16) & 0xFF,
                                               (tintArgb >> 8)  & 0xFF,
                                                tintArgb        & 0xFF));
                g2.fillRect(0, 0, size, size);
            }
            g2.dispose();

            iv.setImage(javafx.embed.swing.SwingFXUtils.toFXImage(bi, null));
        } catch (Exception e) {
            // leave ImageView empty on error
        }
        return iv;
    }
}
