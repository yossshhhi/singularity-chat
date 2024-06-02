module kz.yossshhhi {
    requires javafx.controls;
    requires javafx.fxml;


    opens kz.yossshhhi.client to javafx.fxml;
    exports kz.yossshhhi.client;
}