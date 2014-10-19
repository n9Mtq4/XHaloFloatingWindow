/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/Will/AndroidStudioProjects/XHaloFloatingWindow/app/src/main/aidl/com/zst/xposed/halo/floatingwindow/hooks/ipc/XHFWInterface.aidl
 */
package com.zst.xposed.halo.floatingwindow.hooks.ipc;
public interface XHFWInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.zst.xposed.halo.floatingwindow.hooks.ipc.XHFWInterface
{
private static final java.lang.String DESCRIPTOR = "com.zst.xposed.halo.floatingwindow.hooks.ipc.XHFWInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.zst.xposed.halo.floatingwindow.hooks.ipc.XHFWInterface interface,
 * generating a proxy if needed.
 */
public static com.zst.xposed.halo.floatingwindow.hooks.ipc.XHFWInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.zst.xposed.halo.floatingwindow.hooks.ipc.XHFWInterface))) {
return ((com.zst.xposed.halo.floatingwindow.hooks.ipc.XHFWInterface)iin);
}
return new com.zst.xposed.halo.floatingwindow.hooks.ipc.XHFWInterface.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_bringAppToFront:
{
data.enforceInterface(DESCRIPTOR);
android.os.IBinder _arg0;
_arg0 = data.readStrongBinder();
int _arg1;
_arg1 = data.readInt();
this.bringAppToFront(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_removeAppTask:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
this.removeAppTask(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getLastTaskId:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getLastTaskId();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.zst.xposed.halo.floatingwindow.hooks.ipc.XHFWInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
// Window management

@Override public void bringAppToFront(android.os.IBinder token, int taskId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder(token);
_data.writeInt(taskId);
mRemote.transact(Stub.TRANSACTION_bringAppToFront, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void removeAppTask(int taskId, int flags) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(taskId);
_data.writeInt(flags);
mRemote.transact(Stub.TRANSACTION_removeAppTask, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getLastTaskId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getLastTaskId, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_bringAppToFront = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_removeAppTask = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getLastTaskId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
// Window management

public void bringAppToFront(android.os.IBinder token, int taskId) throws android.os.RemoteException;
public void removeAppTask(int taskId, int flags) throws android.os.RemoteException;
public int getLastTaskId() throws android.os.RemoteException;
}
