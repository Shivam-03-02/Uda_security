module com.udacity.catpoint.security {
    requires java.desktop;
    requires java.prefs;
    requires com.udacity.catpoint.image;
    requires com.google.gson;
    requires com.google.common;

    opens com.udacity.catpoint.data to com.google.gson;
}

