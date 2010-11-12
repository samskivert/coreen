//
// $Id$

package coreen.config;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.NumberTextBox;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;

import coreen.client.AbstractPage;
import coreen.client.Args;
import coreen.client.ClientMessages;
import coreen.client.Page;
import coreen.rpc.LibraryService;
import coreen.rpc.LibraryServiceAsync;
import coreen.util.ClickCallback;
import coreen.util.PanelCallback;

/**
 * Displays global Coreen configuration as well as colophon.
 */
public class ConfigPage extends AbstractPage
{
    public ConfigPage ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    @Override // from AbstractPage
    public Page getId ()
    {
        return Page.CONFIG;
    }

    @Override // from AbstractPage
    public void setArgs (final Args args)
    {
        // load up the metadata for this project
        _settings.setWidget(Widgets.newLabel(_cmsgs.loading()));
        _libsvc.getConfig(new PanelCallback<Map<String, String>>(_settings) {
            public void onSuccess (Map<String, String> config) {
                _settings.setWidget(createContents(config));
            }
        });
    }

    protected Widget createContents (Map<String, String> config)
    {
        FluentTable ctable = new FluentTable(0, 5);
        for (Setting<?, ?> setting : SETTINGS) {
            addSetting(ctable, config, setting);
        }
        return ctable;
    }

    protected <T, E extends Widget> void addSetting (
        FluentTable ctable, final Map<String, String> config, final Setting<T, E> setting)
    {
        final E editor = setting.createEditor(setting.get(config));
        Button update = new Button("Update");
        ctable.add().setText(setting.label).right().setWidget(editor).right().setWidget(update);
        new ClickCallback<Void>(update) {
            protected boolean callService () {
                _value = String.valueOf(setting.getValue(editor));
                _libsvc.updateConfig(setting.key, _value, this);
                return true;
            }
            protected boolean gotResult (Void result) {
                Popups.infoNear("Updated.", getPopupNear());
                config.put(setting.key, _value); // not presently necessary, but good form
                return true;
            }
            protected String _value;
        };
    }

    protected interface Styles extends CssResource
    {
    }
    protected @UiField Styles _styles;
    protected @UiField SimplePanel _settings;

    protected interface Binder extends UiBinder<Widget, ConfigPage> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ConfigMessages _msgs = GWT.create(ConfigMessages.class);
    protected static final ClientMessages _cmsgs = GWT.create(ClientMessages.class);
    protected static final LibraryServiceAsync _libsvc = GWT.create(LibraryService.class);

    protected static abstract class Setting<T, E extends Widget> {
        public final String label;
        public final String key;
        public final T defval;

        public abstract E createEditor (T current);
        public abstract T getValue (E editor);
        public abstract T parseValue (String value);

        public T get (Map<String, String> config) {
            String value = config.get(key);
            return (value == null) ? defval : parseValue(value);
        }

        protected Setting (String label, String key, T defval) {
            this.label = label;
            this.key = key;
            this.defval = defval;
        }
    }

    protected static class BooleanSetting extends Setting<Boolean, CheckBox>
    {
        public BooleanSetting (String label, String key, boolean defval) {
            super(label, key, defval);
        }
        public CheckBox createEditor (Boolean current) {
            CheckBox box = new CheckBox();
            box.setChecked(current);
            return box;
        }
        public Boolean getValue (CheckBox editor) {
            return editor.isChecked();
        }
        public Boolean parseValue (String value) {
            return Boolean.parseBoolean(value);
        }
    }

    protected static class IntSetting extends Setting<Integer, NumberTextBox>
    {
        public IntSetting (String label, String key, int defval) {
            super(label, key, defval);
        }
        public NumberTextBox createEditor (Integer current) {
            NumberTextBox box = NumberTextBox.newIntBox(20);
            box.setNumber(current);
            return box;
        }
        public Integer getValue (NumberTextBox editor) {
            return editor.getNumber().intValue();
        }
        public Integer parseValue (String value) {
            return Integer.parseInt(value);
        }
    }

    protected static class StringSetting extends Setting<String, TextBox>
    {
        public StringSetting (String label, String key, String defval) {
            super(label, key, defval);
        }
        public TextBox createEditor (String current) {
            return Widgets.newTextBox(current, -1, 20);
        }
        public String getValue (TextBox editor) {
            return editor.getText().trim();
        }
        public String parseValue (String value) {
            return value;
        }
    }

    protected static final Setting[] SETTINGS = {
        new StringSetting("HTTP Hostname:", ConfigData.HTTP_HOSTNAME,
                          ConfigData.DEFAULT_HTTP_HOSTNAME),
        new IntSetting("HTTP Port:", ConfigData.HTTP_PORT, ConfigData.DEFAULT_HTTP_PORT),
        // new BooleanSetting("Test boolean:", "test", false),
    };
}
