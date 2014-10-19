package com.zst.xposed.halo.floatingwindow.hooks;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import com.zst.xposed.halo.floatingwindow.Common;
import com.zst.xposed.halo.floatingwindow.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.XModuleResources;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class NotificationShadeHook {

	static boolean mQuickSettingsEnabled;
	static boolean mLongPressEnabled;
	static boolean mSinglePressEnabled = true;
	
	static String TEXT_OPEN_IN_HALO;
	static String TEXT_OPEN_IN_NORMALLY;
	static String TEXT_APP_INFO;
	static String TEXT_FORCE_STOP;
	static String TEXT_ERROR_LAUNCHING;
	
	public static void zygote(XModuleResources module_res) {
		TEXT_APP_INFO = module_res.getString(R.string.notif_app_info);
		TEXT_ERROR_LAUNCHING = module_res.getString(R.string.notif_error_launching);
		TEXT_OPEN_IN_HALO = module_res.getString(R.string.notif_open_halo);
		TEXT_OPEN_IN_NORMALLY = module_res.getString(R.string.notif_open_normal);
		TEXT_FORCE_STOP = module_res.getString(R.string.notif_force_stop);
	}
	public static void hook(final LoadPackageParam lpp, final XSharedPreferences pref) {
		if (!lpp.packageName.equals("com.android.systemui")) return;
		pref.reload();

		mQuickSettingsEnabled = pref.getBoolean(Common.KEY_FLOATING_QUICK_SETTINGS,
				Common.DEFAULT_FLOATING_QUICK_SETTINGS);
		mLongPressEnabled = pref.getBoolean(Common.KEY_NOTIFICATION_LONGPRESS_OPTION,
				Common.DEFAULT_NOTIFICATION_LONGPRESS_OPTION);
		mSinglePressEnabled = pref.getBoolean(Common.KEY_NOTIFICATION_SINGLE_CLICK_HALO,
				Common.DEFAULT_NOTIFICATION_SINGLE_CLICK_HALO);
		
		if (Build.VERSION.SDK_INT <= 15) {
			if (mLongPressEnabled) loadIcsHooks(lpp);
		} else {
			if (mLongPressEnabled) loadNewHooks(lpp);
		}
		if (Build.VERSION.SDK_INT >= 17) {
			try {
				if (mQuickSettingsEnabled) injectQuickSettings(lpp);
			} catch (Throwable t) {
				XposedBridge.log(Common.LOG_TAG + "(QuickSettingsHaloInject)");
				XposedBridge.log(t);
			}
		}
	}
		
	private static void loadNewHooks(final LoadPackageParam lpp) {
		Class<?> baseStatusBar = findClass("com.android.systemui.statusbar.BaseStatusBar",
				lpp.classLoader);
		try {
			injectViewTag(baseStatusBar);
		} catch (Throwable e) {
			XposedBridge.log(Common.LOG_TAG + "(injectViewTag)");
			XposedBridge.log(e);
		}
		
		try {
			hookLongPressNotif(baseStatusBar);
		} catch (Throwable e) {
			XposedBridge.log(Common.LOG_TAG + "(NotificationHook)");
			XposedBridge.log(e);
		}
	}
	
	/* Android 4.0+ (Start) */
	private static void loadIcsHooks(final LoadPackageParam lpp) {
		Class<?> phoneStatusBar = findClass("com.android.systemui.statusbar.phone.PhoneStatusBar",
				lpp.classLoader);
		try {
			injectOldViewTag(phoneStatusBar);
		} catch (Throwable e) {
			XposedBridge.log(Common.LOG_TAG + "(injectViewTag)");
			XposedBridge.log(e);
		}
	}
	
	private static void injectOldViewTag(Class<?> phoneStatusBar) {
		XposedBridge.hookAllMethods(phoneStatusBar, "inflateViews", new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				final Object entry = param.args[0];
				View newRow = (View) XposedHelpers.getObjectField(entry, "row");
				View content = newRow.findViewById(newRow.getResources().getIdentifier(
						"content", "id", "com.android.systemui"));
				content.setTag(entry);

				if (mSinglePressEnabled) {
					content.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
							final Object entry1 = v.getTag();
							final Object sbn = XposedHelpers.getObjectField(entry1, "notification");
							final String packageNameF = (String) XposedHelpers.getObjectField(sbn, "pkg");
							final Notification n = (Notification) XposedHelpers.getObjectField(sbn, "notification");
							
							if (packageNameF == null) return;
							if (v.getWindowToken() == null) return;
							
							try {
								launchFloating(n.contentIntent, v.getContext());
								closeNotificationShade(v.getContext());
							} catch (Exception e) {
								android.widget.Toast.makeText(v.getContext(),
										TEXT_ERROR_LAUNCHING + e.toString(),
										android.widget.Toast.LENGTH_SHORT).show();
							}
							
							} catch (Throwable t) {
							}
						}
					});
				} else {
					content.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								
							final Object entry1 = v.getTag();
							final Object sbn = XposedHelpers.getObjectField(entry1, "notification");
							final String packageNameF = (String) XposedHelpers.getObjectField(sbn, "pkg");
							final Notification n = (Notification) XposedHelpers.getObjectField(sbn, "notification");
							
							final PendingIntent contentIntent = n.contentIntent;
							
							if (packageNameF == null) return;
							if (v.getWindowToken() == null) return;
							
							try {
								launch(new Intent(), contentIntent, v.getContext());
								closeNotificationShade(v.getContext());
							} catch (Exception e) {
								android.widget.Toast.makeText(v.getContext(),
										TEXT_ERROR_LAUNCHING + e.toString(),
										android.widget.Toast.LENGTH_SHORT).show();
							}
							} catch (Throwable t) {
								
							}
						}
					});
				}
				content.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(final View v) {
						try {
							
							final Object entry1 = v.getTag();
							final Object sbn = XposedHelpers.getObjectField(entry1, "notification");
							final String packageNameF = (String) XposedHelpers.getObjectField(sbn, "pkg");
							final Notification n = (Notification) XposedHelpers.getObjectField(sbn, "notification");
							
						if (packageNameF == null) return false;
						if (v.getWindowToken() == null) return false;
						
						try {							
							final Context ctx = v.getContext();
							
							PopupMenu popup = new PopupMenu(ctx, v);
							popup.getMenu().add(TEXT_APP_INFO);
							if (!mSinglePressEnabled) {
								popup.getMenu().add(TEXT_OPEN_IN_HALO);
							} else {
								popup.getMenu().add(TEXT_OPEN_IN_NORMALLY);
							}
							popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
								public boolean onMenuItemClick(MenuItem item) {
									if (item.getTitle().equals(TEXT_APP_INFO)) {
										Intent intent = new Intent(
												Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
												Uri.fromParts("package", packageNameF, null));
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										ctx.startActivity(intent);
										closeNotificationShade(ctx);
									} else if (item.getTitle().equals(TEXT_OPEN_IN_HALO)) {
										launchFloating(n.contentIntent, ctx);
										closeNotificationShade(ctx);
									} else if (item.getTitle().equals(TEXT_OPEN_IN_NORMALLY)) {
										launch(new Intent(), n.contentIntent, ctx);
										closeNotificationShade(ctx);
									} else {
										return false;
									}
									return true;
								}
							});
							popup.show();
							return true;
						} catch (Exception e) {
							return false;
						}
						} catch (Throwable t) {
							return false;
						}
					}
				});
				
				XposedHelpers.setObjectField(entry, "row", newRow);
			}
		});
	}
	/* Android 4.0+ (End) */
	
	/* Android 4.1+ (Start) */
	private static void hookLongPressNotif(Class<?> baseStatusBar) {
		XposedBridge.hookAllMethods(baseStatusBar, "getNotificationLongClicker",
				new XC_MethodReplacement() {
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
				final Object thiz = param.thisObject;
				final Context mContext = (Context) XposedHelpers.findField(
						thiz.getClass(), "mContext").get(thiz);
				return new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(final View v) {
						try {
							final Object entry = v.getTag();							
							final Object sbn = XposedHelpers.getObjectField(entry, "notification");
							final String packageNameF = (String) XposedHelpers.getObjectField(sbn, "pkg");
							final Notification n = (Notification) XposedHelpers.getObjectField(sbn, "notification");
							
							if (packageNameF == null) return false;
							if (v.getWindowToken() == null) return false;

							PopupMenu popup = new PopupMenu(mContext, v);
							popup.getMenu().add(TEXT_APP_INFO);
							if (!mSinglePressEnabled) {
								popup.getMenu().add(TEXT_OPEN_IN_HALO);
							} else {
								popup.getMenu().add(TEXT_OPEN_IN_NORMALLY);
							}
							popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
								public boolean onMenuItemClick(MenuItem item) {
									if (item.getTitle().equals(TEXT_APP_INFO)) {
										Intent intent = new Intent(
												Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
												Uri.fromParts("package", packageNameF,
														null));
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
													mContext.startActivity(intent);
													closeNotificationShade(mContext);
									} else if (item.getTitle().equals(TEXT_OPEN_IN_HALO)) {
										launchFloating(n.contentIntent, mContext);
										closeNotificationShade(mContext);
									} else if (item.getTitle().equals(TEXT_OPEN_IN_NORMALLY)) {
										launch(new Intent(), n.contentIntent, mContext);
										closeNotificationShade(mContext);
									} else {
										return false;
									}
									return true;
								}
							});
							popup.show();
							return true;
						} catch (Exception e) {
							return false;
						}
					}
				};
			}
		});
		
	}
	
	private static void injectViewTag(Class<?> baseStatusBar) {
		XposedBridge.hookAllMethods(baseStatusBar, "inflateViews", new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				final Object entry = param.args[0];
				View newRow = (View) XposedHelpers.getObjectField(entry, "row");
				newRow.setTag(entry);
				if (mSinglePressEnabled) {
					View content = newRow.findViewById(newRow.getResources()
							.getIdentifier("content", "id", "com.android.systemui"));
					content.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View v) {
							try {
								final Object sbn = XposedHelpers.getObjectField(entry, "notification");
								final String packageNameF = (String) XposedHelpers.getObjectField(sbn, "pkg");
								final Notification n = (Notification) XposedHelpers.getObjectField(sbn, "notification");
								
								if (packageNameF == null) return;
								if (v.getWindowToken() == null) return;
								launchFloating(n.contentIntent, v.getContext());
								closeNotificationShade(v.getContext());
							} catch (Exception e) {
								android.widget.Toast.makeText(v.getContext(),
										TEXT_ERROR_LAUNCHING + e.toString(),
										android.widget.Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
				XposedHelpers.setObjectField(entry, "row", newRow);
			}
		});
	}
	/* Android 4.1+ (End) */
	
	/* Android 4.2+ (Start) */
	static Intent stolenIntent;
	private static void injectQuickSettings(final LoadPackageParam lpp) throws Throwable{
		Class<?> qsclass;
		try {
			qsclass = findClass("com.android.systemui.quicksettings.QuickSettingsTile",
					lpp.classLoader);
		} catch (Throwable t) {
			qsclass = findClass("com.android.systemui.statusbar.phone.QuickSettings",
					lpp.classLoader);
		}
		final Class<?> clazz = qsclass;
		findAndHookMethod(clazz, "startSettingsActivity", Intent.class, boolean.class, 
				new XC_MethodHook() {
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				stolenIntent = (Intent) param.args[0];
				param.args[0] = new Intent();
			}
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				final Context ctx = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
				stolenIntent.setFlags(Common.FLAG_FLOATING_WINDOW |
						Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				ctx.startActivity(stolenIntent);
			}
		});
	}
	/* Android 4.2+ (End) */
	
	private static void launchFloating(PendingIntent pIntent, Context mContext) { 
		Intent intent = new Intent();
		intent.addFlags(Common.FLAG_FLOATING_WINDOW);
		launch(intent, pIntent, mContext);
	}
	private static void launch(Intent intent, PendingIntent pIntent, Context mContext) { 
		if (pIntent == null) return;
		try {
			Class<?> classAMN = XposedHelpers.findClass("android.app.ActivityManagerNative", null);
			Object iAM = /* IActivityManager */ XposedHelpers.callStaticMethod(classAMN, "getDefault");
			XposedHelpers.callMethod(iAM, "resumeAppSwitches");
			XposedHelpers.callMethod(iAM, "dismissKeyguardOnNextActivity");
			// android.app.ActivityManagerNative.getDefault().resumeAppSwitches();
			// android.app.ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
			pIntent.send(mContext, 0, intent);
		} catch (Exception e) {
			android.widget.Toast.makeText(mContext, TEXT_ERROR_LAUNCHING + e.toString(),
					android.widget.Toast.LENGTH_SHORT).show();
		}
	}
	
	private static void closeNotificationShade(Context c) {
		final Object statusBar = /* StatusBarManager */ c.getSystemService("statusbar");
		if (statusBar == null) return;
		try {
			XposedHelpers.callMethod(statusBar, "collapse");
		} catch (Throwable e) { // OEM's might remove this expand method.
			try { // 4.2.2 (later builds) changed method name
				XposedHelpers.callMethod(statusBar, "collapsePanels");
			} catch (Throwable e2) { // else No Hope! Just leave it :P
			}
		}
	}	
}
