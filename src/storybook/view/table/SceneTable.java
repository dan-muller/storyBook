/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package storybook.view.table;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.hibernate.Session;
import storybook.SbConstants;
import storybook.SbConstants.ViewName;
import storybook.controller.DocumentController;
import storybook.model.DocumentModel;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.model.hbn.entity.Scene;
import storybook.model.state.SceneState;
import storybook.model.state.SceneStateModel;
import storybook.toolkit.I18N;
import storybook.view.MainFrame;
import storybook.view.SbColumnFactory;
import storybook.view.combo.SceneStateComboModel;
import storybook.view.combo.SceneStateListCellRenderer;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class SceneTable extends AbstractTable {

	private JComboBox sceneStateCombo;

	public SceneTable(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void init() {
		columns = SbColumnFactory.getInstance().getSceneColumns();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<AbstractEntity> getAllEntities() {
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		SceneDAOImpl dao = new SceneDAOImpl(session);
		SceneState state = (SceneState) sceneStateCombo.getSelectedItem();
		if (state.getNumber().equals(SceneStateModel.State.ALL.ordinal())) {
			List<?> ret = dao.findAll();
			model.commit();
			return (List<AbstractEntity>) ret;
		}
		List<?> ret2 = dao.findBySceneState(state);
		model.commit();
		return (List<AbstractEntity>) ret2;
	}

	@Override
	protected void initOptionsPanel() {
		JLabel lbState = new JLabel(I18N.getMsgColon("msg.status"));
		sceneStateCombo = new JComboBox(new SceneStateComboModel());
		sceneStateCombo.setName(SbConstants.ComponentName.COMBO_SCENE_STATES
				.toString());
		// set "show all"
		sceneStateCombo.setSelectedIndex(6);
		sceneStateCombo.setRenderer(new SceneStateListCellRenderer());
		sceneStateCombo.addActionListener(this);
		optionsPanel.add(lbState);
		optionsPanel.add(sceneStateCombo);
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox) e.getSource();
			if (SbConstants.ComponentName.COMBO_SCENE_STATES.toString().equals(
					cb.getName())) {
				SceneState state = (SceneState) sceneStateCombo
						.getSelectedItem();
				mainFrame.getDocumentController().filterScenes(state);
				return;
			}
		}
		super.actionPerformed(e);
	}

	@Override
	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		try {
			String propName = evt.getPropertyName();
			Object newValue = evt.getNewValue();
//			Object oldValue = evt.getOldValue();
			if (DocumentController.SceneProps.INIT.check(propName)) {
				initTableModel(evt);
				return;
			}
			if (DocumentController.SceneProps.UPDATE.check(propName)) {
				updateEntity(evt);
				return;
			}
			if (DocumentController.SceneProps.NEW.check(propName)) {
				newEntity(evt);
				return;
			}
			if (DocumentController.SceneProps.DELETE.check(propName)) {
				deleteEntity(evt);
				return;
			}
			if (DocumentController.SceneProps.FILTER.check(propName)) {
				initTableModel(evt);
				if (newValue instanceof SceneState) {
					sceneStateCombo.setSelectedItem((SceneState) newValue);
				}
//				return;
			}

			// show only scenes from current part
//			if (DocumentController.PartProps.CHANGE.check(propName)) {
//				DocumentModel model = mainFrame.getDocumentModel();
//				Session session = model.beginTransaction();
//				SceneDAOImpl dao = new SceneDAOImpl(session);
//				Part curPart = mainFrame.getCurrentPart();
//				List<Scene> scenes = dao.findByPart(curPart);
//				model.commit();
//				PropertyChangeEvent evt2 = new PropertyChangeEvent(
//						evt.getSource(), evt.getPropertyName(), null, scenes);
//				initTableModel(evt2);
//			}
		} catch (Exception e) {
		}
	}

	@Override
	protected void sendSetEntityToEdit(int row) {
		if (row == -1) {
			return;
		}
		Scene scene = (Scene) getEntityFromRow(row);
		ctrl.setSceneToEdit(scene);
		mainFrame.showView(ViewName.EDITOR);
	}

	@Override
	protected void sendSetNewEntityToEdit(AbstractEntity entity) {
		ctrl.setSceneToEdit((Scene) entity);
		mainFrame.showView(ViewName.EDITOR);
	}

	@Override
	protected synchronized void sendDeleteEntity(int row) {
		Scene scene = (Scene) getEntityFromRow(row);
		ctrl.deleteScene(scene);
	}

	@Override
	protected synchronized void sendDeleteEntities(int[] rows) {
		ArrayList<Long> ids = new ArrayList<Long>();
		for (int row : rows) {
			Scene scene = (Scene) getEntityFromRow(row);
			ids.add(scene.getId());
		}
		ctrl.deleteMultiScenes(ids);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		SceneDAOImpl dao = new SceneDAOImpl(session);
		Scene scene = dao.find(id);
		model.commit();
		return scene;
	}

	@Override
	protected AbstractEntity getNewEntity() {
		return new Scene();
	}
}
