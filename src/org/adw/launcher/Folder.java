/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.adw.launcher;


import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * Represents a set of icons chosen by the user or generated by the system.
 */
public class Folder extends LinearLayout implements DragSource, OnItemLongClickListener,
        OnItemClickListener, OnClickListener, View.OnLongClickListener {

    protected AbsListView mContent;
    protected DragController mDragger;
    
    protected Launcher mLauncher;

    protected Button mCloseButton;
    
    protected FolderInfo mInfo;
    
    /**
     * Which item is being dragged
     */
    protected ApplicationInfo mDragItem;
    /**
     * ADW:Theme vars
     */
    private int mTextColor=0;
    private boolean useThemeTextColor=false;

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public Folder(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAlwaysDrawnWithCacheEnabled(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContent = (AbsListView) findViewById(R.id.folder_content);
        mContent.setOnItemClickListener(this);
        mContent.setOnItemLongClickListener(this);
        
        mCloseButton = (Button) findViewById(R.id.folder_close);
        mCloseButton.setOnClickListener(this);
        mCloseButton.setOnLongClickListener(this);
    	//ADW: Load the specified theme
    	String themePackage=AlmostNexusSettingsHelper.getThemePackageName(getContext(), Launcher.THEME_DEFAULT);
    	PackageManager pm=getContext().getPackageManager();
    	Resources themeResources=null;
    	if(!themePackage.equals(Launcher.THEME_DEFAULT)){
	    	try {
				themeResources=pm.getResourcesForApplication(themePackage);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
    	}
		if(themeResources!=null){
			//Action Buttons
			Launcher.loadThemeResource(themeResources,themePackage,"box_launcher_top",mCloseButton,Launcher.THEME_ITEM_BACKGROUND);
			Launcher.loadThemeResource(themeResources,themePackage,"box_launcher_bottom",mContent,Launcher.THEME_ITEM_BACKGROUND);
			int grid_selector_id=themeResources.getIdentifier("grid_selector", "drawable", themePackage);
			if(grid_selector_id!=0){
				mContent.setSelector(themeResources.getDrawable(grid_selector_id));
			}
			int textColorId=themeResources.getIdentifier("folder_title_color", "color", themePackage);
			if(textColorId!=0){
				mTextColor=themeResources.getColor(textColorId);
				mCloseButton.setTextColor(mTextColor);
			}
			Typeface themeFont=Typeface.createFromAsset(themeResources.getAssets(), "themefont.ttf");
			if(themeFont!=null)mCloseButton.setTypeface(themeFont);
		}
        
    }
    
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);
		// set bound
		if (v != null) {
		    Rect targetRect = new Rect();
		    v.getGlobalVisibleRect(targetRect);
		    try{
		    	app.intent.setSourceBounds(targetRect);
		    }catch(NoSuchMethodError e){};
		}        
        mLauncher.startActivitySafely(app.intent);
        if (mLauncher.autoCloseFolder) {
            mLauncher.closeFolder(this);
        }
    }

    public void onClick(View v) {
        mLauncher.closeFolder(this);
    }

    public boolean onLongClick(View v) {
        mLauncher.closeFolder(this);
        mLauncher.showRenameDialog(mInfo);
        return true;
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!view.isInTouchMode()) {
            return false;
        }

        ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);

        mDragger.startDrag(view, this, app, DragController.DRAG_ACTION_COPY);
        mLauncher.closeFolder(this);
        mDragItem = app;

        return true;
    }

    public void setDragger(DragController dragger) {
        mDragger = dragger;
    }

    public void onDropCompleted(View target, boolean success) {
    }

    /**
     * Sets the adapter used to populate the content area. The adapter must only
     * contains ApplicationInfo items.
     *
     * @param adapter The list of applications to display in the folder.
     */
    void setContentAdapter(BaseAdapter adapter) {
        mContent.setAdapter(adapter);
    }

    void notifyDataSetChanged() {
        ((BaseAdapter) mContent.getAdapter()).notifyDataSetChanged();
    }

    void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }
    
    /**
     * @return the FolderInfo object associated with this folder
     */
    FolderInfo getInfo() {
        return mInfo;
    }

    // When the folder opens, we need to refresh the GridView's selection by
    // forcing a layout
    void onOpen() {
        mContent.requestLayout();
    }

    void onClose() {
        final Workspace workspace = mLauncher.getWorkspace();
        workspace.getChildAt(workspace.getCurrentScreen()).requestFocus();
    }

    void bind(FolderInfo info) {
        mInfo = info;
        mCloseButton.setText(info.title);
    }
}
