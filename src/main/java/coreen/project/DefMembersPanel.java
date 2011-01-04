//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.FluentTable;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;
import com.threerings.gwt.util.WindowUtil;

import coreen.client.Args;
import coreen.client.Link;
import coreen.client.Page;
import coreen.model.DefInfo;
import coreen.model.Kind;
import coreen.model.MemberInfo;
import coreen.model.Project;
import coreen.ui.WindowFX;
import coreen.util.DefMap;
import coreen.util.IdMap;
import coreen.util.PanelCallback;

/**
 * Displays the members of a single module.
 */
public class DefMembersPanel extends AbstractProjectPanel
{
    public DefMembersPanel ()
    {
        initWidget(_binder.createAndBindUi(this));
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.MEM;
    }

    @Override // from AbstractProjectPanel
    public void setArgs (Project proj, Args args)
    {
        _proj = proj;
        _projsvc.getMemberInfo(args.get(2, 0l), new PanelCallback<MemberInfo>(_contents) {
            public void onSuccess (MemberInfo members) {
                _contents.clear();
                gotMembers(members);
            }
        });
    }

    protected void gotMembers (MemberInfo info)
    {
        if (info.doc != null) {
            _contents.add(new DocLabel(info.doc));
        }
        _contents.add(Widgets.newLabel(info.name, _styles.title()));

        FluentTable contents = new FluentTable(2, 0, _styles.grid());
        for (DefInfo member : info.members) {
            Widget link = Link.createInline(member.name, Page.PROJECT, _proj.id,
                                            ProjectPage.Detail.forKind(member.kind), member.id);
            FluentTable.Cell cell = contents.add().alignTop().
                setWidget(DefUtil.adornDef(member, link), _styles.memberCell());
            if (member.doc != null) {
                cell.right().setWidget(new DocLabel(member.doc), _styles.memberCell());
            } else {
                cell.right().setHTML("&nbsp;", _styles.memberCell()); // preserve formatting
            }
        }
        _contents.add(contents);
    }

    protected interface Styles extends CssResource
    {
        String title ();
        String grid ();
        String memberCell ();
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _contents;

    protected Project _proj;
    protected DefMap _defmap = new DefMap();

    protected interface Binder extends UiBinder<Widget, DefMembersPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
}
