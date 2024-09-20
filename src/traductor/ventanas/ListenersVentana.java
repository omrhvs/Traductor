package traductor.ventanas;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;

public class ListenersVentana 
{
    public static void reposicionarVentanaListener(JFrame ventana)
    {
        final Point[] clickInicial = new Point[1];

        ventana.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                clickInicial[0] = e.getPoint();
            }
        }
        );

        ventana.addMouseMotionListener(new MouseAdapter()
        {
           public void mouseDragged(MouseEvent e)
            {
                int thisX = ventana.getLocation().x;
                int thisY = ventana.getLocation().y;

                int xMoved = e.getX() - clickInicial[0].x;
                int yMoved = e.getY() - clickInicial[0].y;

                int x = thisX + xMoved;
                int y = thisY + yMoved;

                ventana.setLocation(x, y);
            }
        }
        );
    }
}
