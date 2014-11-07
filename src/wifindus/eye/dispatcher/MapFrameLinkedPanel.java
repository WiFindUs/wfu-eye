package wifindus.eye.dispatcher;

import javax.swing.JPanel;

import wifindus.eye.MapFrame;
import wifindus.eye.MappableObject;

public class MapFrameLinkedPanel extends JPanel
{
	private static final long serialVersionUID = 2083630201718155760L;
	private transient final MapFrame mapFrame;
	
    public MapFrameLinkedPanel(MapFrame mapFrame)
    {
		if (mapFrame == null)
			throw new NullPointerException("Parameter 'mapFrame' cannot be null.");
		this.mapFrame = mapFrame;
    }
    
	public MapFrame getMapFrame()
	{
		return mapFrame;
	}
	
	protected void locateObjectOnMap(final MappableObject object)
	{
		if (object == null)
			return;
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (!mapFrame.isShowing())
					mapFrame.setVisible(true);
				mapFrame.toFront();
				mapFrame.setPan(object, true);
				mapFrame.setZoom(1.5, true);
			}
		});
	}
}
