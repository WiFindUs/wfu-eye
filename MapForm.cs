using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace WFU_Eye_Mockup
{
	public partial class MapForm : Form
	{
		private MapProvider provider = null;
		private static Pen gridPen = null;
		private static Brush darkenBrush, gridTextBrush = null;

		public MapForm(MapProvider provider)
		{
			InitializeComponent();
			this.provider = provider;
		}
		public MapForm() : this(null) { }

		/// <summary>
		/// Translates a GPS position to pixel coordinates on the map
		/// </summary>
		private Point MapCoords(Position p)
		{
			if (this.provider == null)
				return new Point(0,0);
			return new Point(
				(int)(((p.Longitude - provider.LongitudeStart) / (provider.LongitudeEnd - provider.LongitudeStart)) * (decimal)mapImage.Width),
				(int)(((p.Latitude - provider.LatitudeStart) / (provider.LatitudeEnd - provider.LatitudeStart)) * (decimal)mapImage.Height)
				);
		}

		private void mapImage_Paint(object sender, PaintEventArgs e)
		{
			if (provider == null)
				return;

			//darken the background image a bit
			if (darkenBrush == null)
				darkenBrush = new SolidBrush(Color.FromArgb(100, 0, 0, 0));
			e.Graphics.FillRectangle(darkenBrush, 0, 0, mapImage.Width, mapImage.Height);

			//draw grid
			if (gridPen == null)
				gridPen = new Pen(Color.FromArgb(125, 255, 255, 255), 1.0f);
			if (gridTextBrush == null)
				gridTextBrush = new SolidBrush(Color.FromArgb(125, 255, 255, 255));
			int wStep = mapImage.Width/10;
			int hStep = mapImage.Height/10;
			for (int i = 0; i < 10; i++)
			{
				//horizontal lines (rows)
				e.Graphics.DrawLine(gridPen, 0, hStep * (i + 1), mapImage.Width, hStep * (i + 1));
				String s = ((char)('A' + i)).ToString();
				SizeF size = e.Graphics.MeasureString(s, mapImage.Font);
				e.Graphics.DrawString(s, mapImage.Font, gridTextBrush, 2, hStep / 2 + (hStep * i) - size.Height/2);

				//vertical lines (columns)
				e.Graphics.DrawLine(gridPen, wStep * (i + 1), 0, wStep * (i + 1), mapImage.Height);
				s = (i + 1).ToString();
				size = e.Graphics.MeasureString(s, mapImage.Font);
				e.Graphics.DrawString(s, mapImage.Font, gridTextBrush, wStep / 2 + (wStep * i) - size.Width / 2, 2);
			}

			//draw personnel
			foreach (Personnel person in provider.AllPersonnel)
			{
				//translate the gps coordinate to map position
				Point pt = MapCoords(person);

				//point marker
				Image icon = person.Icon;
				int spacer = 5;
				string s = (person.ID).Replace("&&", "&");
				SizeF size = e.Graphics.MeasureString(s, mapImage.Font);
				int idWidth = (int)(size.Width + 6.0f + (float)icon.Width);
				int idHeight = (int)(Math.Max(size.Height, icon.Height) + 4.0f);
				Point[] poly = new Point[] { //draws a little arrow-shaped marker
					new Point(pt.X,pt.Y),
					new Point(pt.X-5, pt.Y-spacer),
					new Point(pt.X-idWidth/2, pt.Y-spacer),
					new Point(pt.X-idWidth/2, pt.Y-spacer-idHeight),
					new Point(pt.X+idWidth/2, pt.Y-spacer-idHeight),
					new Point(pt.X+idWidth/2, pt.Y-spacer),
					new Point(pt.X+5, pt.Y-spacer)
				};
				e.Graphics.FillPolygon(Brushes.White, poly);
				e.Graphics.DrawPolygon(Pens.Black, poly);
				e.Graphics.DrawImage(icon, pt.X - idWidth / 2 + 2, pt.Y - spacer - (idHeight / 2) - (icon.Height / 2));
				e.Graphics.DrawString(s, mapImage.Font, Brushes.Black, pt.X - idWidth / 2 + 4 + icon.Width, pt.Y - spacer - (idHeight / 2) - (size.Height / 2));
			}
		}

	}
}

