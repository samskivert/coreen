//
// $Id$

package coreen.server

import java.awt.{Desktop, MenuItem, PopupMenu, SystemTray, TrayIcon}
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

trait TrayComponent extends Component {
  this :Log with Http =>

  implicit private def toActionListener (action : =>Unit) :ActionListener = new ActionListener {
    def actionPerformed (e :ActionEvent) {
      action
    }
  }

  override protected def startComponents {
    super.startComponents

    try {
      // install the system tray menu and whatnot
      if (SystemTray.isSupported) {
        val popup = new PopupMenu
        val showProjects :ActionListener = Desktop.getDesktop.browse(getServerURL("").toURI)
        popup.add(newMenuItem("Show projects...", showProjects))
        popup.add(newMenuItem("Quit", Coreen.shutdown))

        val icon = ImageIO.read(getClass.getClassLoader.getResource("coreen/trayicon.png"))
        val size = SystemTray.getSystemTray.getTrayIconSize
        val image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB)
        val gfx = image.createGraphics
        gfx.drawImage(icon, (size.width - icon.getWidth)/2,
                      (size.height - icon.getHeight)/2, null)
        gfx.dispose

        _tricon = new TrayIcon(image, "Coreen", popup)
        _tricon.addMouseListener(new MouseAdapter {
          override def mouseClicked (e :MouseEvent) {
            showProjects.actionPerformed(null)
          }
        })
        SystemTray.getSystemTray.add(_tricon)

      } else {
        _log.info("System tray not supported. No tray icon for you!")
      }

    } catch {
      case e => _log.warning("Failed to initialize tray icon: " + e)
    }
  }

  override protected def shutdownComponents {
    super.shutdownComponents

    try {
      if (_tricon != null) {
        SystemTray.getSystemTray.remove(_tricon)
      }
    } catch {
      case e => _log.warning("Error removing tray icon: " + e)
    }
  }

  private def newMenuItem (label :String, listener :ActionListener) = {
    val item = new MenuItem(label)
    item.addActionListener(listener)
    item
  }

  private var _tricon :TrayIcon = _
}
