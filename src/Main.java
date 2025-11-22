import com.formdev.flatlaf.FlatDarculaLaf;
import view.ManagementFrame;

import javax.swing.*;

void main() {

    FlatDarculaLaf.setup();

    SwingUtilities.invokeLater(ManagementFrame::new);

}
