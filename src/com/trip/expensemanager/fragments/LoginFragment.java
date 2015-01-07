package com.trip.expensemanager.fragments;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.trip.expensemanager.CloudEndpointUtils;
import com.trip.expensemanager.ProcessingActivity;
import com.trip.expensemanager.R;
import com.trip.expensemanager.loginendpoint.Loginendpoint;
import com.trip.expensemanager.loginendpoint.model.CollectionResponseLogIn;
import com.trip.utils.Constants;
import com.trip.utils.Global;

public class LoginFragment extends CustomFragment implements OnClickListener, AnimationListener {

	private static final int REQUEST_CODE_LOGIN = 1;
	private static final int REQUEST_CODE_REGISTER = 2;

	public static LoginFragment newInstance() {
		LoginFragment fragment=null;
		try {
			fragment=new LoginFragment();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fragment;
	}

	private TextView txtNewUser;
	private EditText eTxtLoginUsername;
	private EditText eTxtLoginPassword;
	private EditText eTxtRegisterUsername;
	private EditText eTxtRegisterPassword;
	private EditText eTxtConfPassword;
	private Button btnLogin;
	private Button btnRegister;
	private String strUsername;
	private String strPassword;
	private String strConfPassword;
	protected AlertDialog alert;
	private ImageView ivIcon;
	private ScrollView svLogin, svRegister;
	protected Animation bounceImage;
	protected Animation exitAnim;
	protected Animation enterAnim;
	private ProgressBar pbUsername;
	private ProgressBar pbConfPwd;
	protected boolean pwdChecked=false;
	protected boolean usernameChecked=false;
	private Animation registerAnim;
	private Animation regEnterAnim;
	private TextView txtBack;
	private Animation loginAnim;
	private int position=0;
	private FragmentActivity context;
	private boolean isVersionICSorGreater=true;
	private EditText eTxtPrefferedName;
	protected boolean prefferedNameChecked;
	protected String strPrefferedName;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView=null;
		super.onCreateView(inflater, container, savedInstanceState);
		try{
			isVersionICSorGreater=android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
			this.context=getActivity();
			rootView= inflater.inflate(R.layout.fragment_expense_login,container, false);

			ivIcon=(ImageView) rootView.findViewById(R.id.iv_icon);
			svLogin=(ScrollView) rootView.findViewById(R.id.sv_login);
			svRegister=(ScrollView) rootView.findViewById(R.id.sv_register);
			txtNewUser=(TextView) rootView.findViewById(R.id.txt_new_user);
			eTxtLoginUsername=(EditText) rootView.findViewById(R.id.etxt_login_username);
			eTxtLoginPassword=(EditText) rootView.findViewById(R.id.etxt_login_password);
			btnLogin=(Button) rootView.findViewById(R.id.btn_login);
			eTxtRegisterUsername=(EditText) rootView.findViewById(R.id.etxt_register_username);
			eTxtPrefferedName=(EditText) rootView.findViewById(R.id.etxt_preffered_name);
			eTxtRegisterPassword=(EditText) rootView.findViewById(R.id.etxt_register_password);
			eTxtConfPassword=(EditText) rootView.findViewById(R.id.etxt_conf_password);
			btnRegister=(Button) rootView.findViewById(R.id.btn_register);
			pbUsername=(ProgressBar) rootView.findViewById(R.id.pb_username);
			pbConfPwd=(ProgressBar) rootView.findViewById(R.id.pb_conf_password);
			txtBack=(TextView) rootView.findViewById(R.id.txt_back);
			if(!isVersionICSorGreater){
				position=1;
			}
			if(savedInstanceState!=null){
				position=savedInstanceState.getInt("position");
			}
			if(position==0){
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						if(isVersionICSorGreater){
							bounceImage = AnimationUtils.loadAnimation(context, R.anim.down_n_bounce);
							bounceImage.setInterpolator(new BounceInterpolator());
						} else{
							bounceImage = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
						}
						bounceImage.setAnimationListener(LoginFragment.this);
						ivIcon.setVisibility(View.VISIBLE);
						ivIcon.startAnimation(bounceImage);
					}
				}, 1000);
			} else if(position==1){
				ivIcon.setVisibility(View.GONE);
				svLogin.setVisibility(View.VISIBLE);
				svRegister.setVisibility(View.INVISIBLE);
			} else if(position==2){
				ivIcon.setVisibility(View.GONE);
				svLogin.setVisibility(View.INVISIBLE);
				svRegister.setVisibility(View.VISIBLE);
			}

			btnLogin.setOnClickListener(this);
			txtBack.setOnClickListener(this);
			txtNewUser.setOnClickListener(this);

			eTxtLoginUsername.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					strPassword=eTxtLoginPassword.getText().toString();
					strUsername=eTxtLoginUsername.getText().toString();
					if(!strPassword.equals("") && !strUsername.equals("")){
						btnLogin.setEnabled(true);
					} else{
						btnLogin.setEnabled(false);
					}
				}
			});

			eTxtLoginPassword.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					strPassword=eTxtLoginPassword.getText().toString();
					strUsername=eTxtLoginUsername.getText().toString();
					if(!strPassword.equals("") && !strUsername.equals("")){
						btnLogin.setEnabled(true);
					} else{
						btnLogin.setEnabled(false);
					}
				}
			});

			eTxtRegisterUsername.addTextChangedListener(new TextWatcher() {


				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					eTxtRegisterUsername.setError(null);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if(Global.isConnected(context)){
						strUsername=eTxtRegisterUsername.getText().toString();
						btnRegister.setEnabled(false);
						pbUsername.setVisibility(View.VISIBLE);
						usernameChecked=false;
						if(strUsername!=null && !strUsername.equals("")){
							new Thread(new Runnable() {

								@Override
								public void run() {
									String userName=strUsername;
									CollectionResponseLogIn result = null;
									try {
										Thread.sleep(2000);
										if(userName.equals(strUsername)){
											Loginendpoint.Builder endpointBuilder = new Loginendpoint.Builder(
													AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
											endpointBuilder = CloudEndpointUtils.updateBuilder(endpointBuilder);
											Loginendpoint endpoint = endpointBuilder.build();

											try {
												result = endpoint.listLogIn().setUsername(userName).execute();
												if (result == null || result.getItems() == null || result.getItems().size() < 1) {
													if(pwdChecked && prefferedNameChecked){
														context.runOnUiThread(new Runnable() {
															public void run() {
																btnRegister.setEnabled(true);
																pbUsername.setVisibility(View.INVISIBLE);
															}
														});
													} else{
														context.runOnUiThread(new Runnable() {
															public void run() {
																pbUsername.setVisibility(View.INVISIBLE);
															}
														});
													}
													usernameChecked=true;
												} else{
													context.runOnUiThread(new Runnable() {
														public void run() {
															Drawable d= context.getResources().getDrawable(R.drawable.error);
															d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
															eTxtRegisterUsername.setError("Username exists!!", d);
															pbUsername.setVisibility(View.INVISIBLE);
														}
													});
												}
											} catch (IOException e) {
												e.printStackTrace();
												result = null;
												context.runOnUiThread(new Runnable() {
													public void run() {
														showMessage("Unable to contact the server. Please try again later!!");
													}
												});
												pbUsername.setVisibility(View.INVISIBLE);
											}
										}
									} catch(Exception e){
										e.printStackTrace();
									}
								}
							}).start();
						} else{
							context.runOnUiThread(new Runnable() {
								public void run() {
									eTxtRegisterUsername.setError(null);
									pbUsername.setVisibility(View.INVISIBLE);
								}
							});
						}
					} else{
						context.runOnUiThread(new Runnable() {
							public void run() {
								showMessage("Unable to contact the server. Please try again later!!");
							}
						});
					}
				}
			});
			
			eTxtPrefferedName.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					btnRegister.setEnabled(false);
					eTxtPrefferedName.setError(null);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					strPrefferedName=eTxtPrefferedName.getText().toString().trim();
					if(!strPrefferedName.equals("")){
						prefferedNameChecked=true;
						if(prefferedNameChecked && pwdChecked && usernameChecked){
							btnRegister.setEnabled(true);
						}
					}
				}
			});

			eTxtRegisterPassword.addTextChangedListener(new TextWatcher() {


				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					eTxtConfPassword.setError(null);
					pbConfPwd.setVisibility(View.INVISIBLE);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					strPassword=eTxtRegisterPassword.getText().toString();
					strConfPassword=eTxtConfPassword.getText().toString();
					btnRegister.setEnabled(false);
					pbConfPwd.setVisibility(View.VISIBLE);
					pwdChecked=false;
					if(strPassword!=null && !strPassword.equals("") && strConfPassword!=null && !strConfPassword.equals("")){
						new Thread(new Runnable() {

							@Override
							public void run() {
								String strPasswordTemp = strPassword;
								try {
									Thread.sleep(2000);
									if(strPasswordTemp.equals(strPassword)){
										if(!strPassword.equals(strConfPassword)){
											context.runOnUiThread(new Runnable() {

												@Override
												public void run() {
													Drawable d= context.getResources().getDrawable(R.drawable.error);
													d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
													eTxtConfPassword.setError("Passwords should match!!", d);
													pbConfPwd.setVisibility(View.INVISIBLE);
												}
											});
										} else{
											context.runOnUiThread(new Runnable() {

												@Override
												public void run() {
													eTxtConfPassword.setError(null);
													pwdChecked=true;
													if(usernameChecked && prefferedNameChecked){
														btnRegister.setEnabled(true);
													}
													pbConfPwd.setVisibility(View.INVISIBLE);
												}
											});
										}
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}).start();
					} else{
						eTxtConfPassword.setError(null);
						pbConfPwd.setVisibility(View.INVISIBLE);
					}

				}
			});

			eTxtConfPassword.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					eTxtConfPassword.setError(null);
					pbConfPwd.setVisibility(View.INVISIBLE);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					strPassword=eTxtRegisterPassword.getText().toString();
					strConfPassword=eTxtConfPassword.getText().toString();
					btnRegister.setEnabled(false);
					pbConfPwd.setVisibility(View.VISIBLE);
					if(strConfPassword!=null && !strConfPassword.equals("")){
						pwdChecked=false;
						new Thread(new Runnable() {

							@Override
							public void run() {
								String strConfPasswordTemp = strConfPassword;
								try {
									Thread.sleep(2000);
									if(strConfPasswordTemp.equals(strConfPassword)){
										if(!strPassword.equals(strConfPassword)){
											context.runOnUiThread(new Runnable() {

												@Override
												public void run() {
													Drawable d= context.getResources().getDrawable(R.drawable.error);
													d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
													eTxtConfPassword.setError("Passwords should match!!", d);
													pbConfPwd.setVisibility(View.INVISIBLE);
												}
											});
										} else{
											context.runOnUiThread(new Runnable() {

												@Override
												public void run() {
													eTxtConfPassword.setError(null);
													pwdChecked=true;
													if(usernameChecked && prefferedNameChecked){
														btnRegister.setEnabled(true);
													}
													pbConfPwd.setVisibility(View.INVISIBLE);
												}
											});
										}
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}).start();
					} else{
						eTxtConfPassword.setError(null);
					}

				}
			});

			btnRegister.setOnClickListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}



	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("position", position);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v) {
		if(v.equals(txtNewUser)){
			pwdChecked=false;
			if(isVersionICSorGreater){
				registerAnim = AnimationUtils.loadAnimation(context, R.anim.down_n_hide);
				registerAnim.setInterpolator(new AnticipateOvershootInterpolator());
			} else{
				registerAnim=AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
			}
			registerAnim.setAnimationListener(LoginFragment.this);
			svLogin.startAnimation(registerAnim);
		} else if(v.equals(txtBack)){
			pwdChecked=false;
			if(isVersionICSorGreater){
				loginAnim = AnimationUtils.loadAnimation(context, R.anim.down_n_hide);
				loginAnim.setInterpolator(new AnticipateOvershootInterpolator());
			} else{
				loginAnim=AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
			}
			loginAnim.setAnimationListener(LoginFragment.this);
			svRegister.startAnimation(loginAnim);
		} else if(v.equals(btnLogin)){
			if(Global.isConnected(context)){
				if(Global.validate(eTxtLoginUsername, eTxtLoginPassword)){
					strUsername=eTxtLoginUsername.getText().toString();
					strPassword=eTxtLoginPassword.getText().toString();
					login();
				} 
			} else{
				showMessage("Looks like you are not connected to internet!!");
			}
		} else if(v.equals(btnRegister)){
			if(Global.isConnected(context)){
				if(Global.validate(eTxtRegisterUsername, eTxtRegisterPassword, eTxtConfPassword, eTxtPrefferedName)){

					strUsername=eTxtRegisterUsername.getText().toString().trim();
					strPassword=eTxtRegisterPassword.getText().toString().trim();
					strConfPassword=eTxtConfPassword.getText().toString().trim();
					strPrefferedName=eTxtPrefferedName.getText().toString().trim();
					if(!strPassword.equals(strConfPassword)){
						Drawable d= context.getResources().getDrawable(R.drawable.error);
						d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
						eTxtConfPassword.setError("Passwords should match!!", d);
					} else if(strPrefferedName.equalsIgnoreCase(Constants.STR_YOU)){
						Drawable d= context.getResources().getDrawable(R.drawable.error);
						d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
						eTxtPrefferedName.setError("You cannot use this screen name!!", d);
					} else{
						registerUser();
					}
				}
			} else{
				showMessage("Looks like you are not connected to internet!!");
			}
		}
	}

	private void registerUser() {
		try {
			String[] usrnamePwd=new String[]{strUsername, strPassword, strPrefferedName};
			Intent intent=new Intent(context, ProcessingActivity.class);
			intent.putExtra(Constants.STR_DATA, usrnamePwd);
			intent.putExtra(Constants.STR_REQUEST, Constants.I_OPCODE_REGISTER);
			startActivityForResult(intent, REQUEST_CODE_REGISTER);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void login() {
		try {
			String[] usrnamePwd=new String[]{strUsername, strPassword};
			Intent intent=new Intent(context, ProcessingActivity.class);
			intent.putExtra(Constants.STR_DATA, usrnamePwd);
			intent.putExtra(Constants.STR_REQUEST, Constants.I_OPCODE_LOGIN);
			startActivityForResult(intent, REQUEST_CODE_LOGIN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			if(requestCode==REQUEST_CODE_LOGIN){
				if(resultCode==Activity.RESULT_OK){
					int iResult=data.getIntExtra(Constants.STR_RESULT, -1);
					String strError=data.getStringExtra(Constants.STR_ERROR);
					if(iResult==1){
						long lngUserId=data.getLongExtra(Constants.STR_USER_ID,0L);
						if(lngUserId!=0L){
							context.getSupportFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
							context.getSupportFragmentManager().beginTransaction().replace(R.id.container, AddTripFragment.newInstance(lngUserId)).commit();
						} else {
							showMessage("Something went wrong!!");
						}
					} else if(iResult==3){
						showGMSNotFoundDialog();

					} else if(strError!=null){
						showMessage(strError);
					} else{
						showMessage("Login unsuccessful!!");
					}
					Log.d("Expense", "Error is "+strError);
					Log.d("Expense", "*************************Success***************************");
				} else{

				}
			} else if(requestCode==REQUEST_CODE_REGISTER){
				if(resultCode==Activity.RESULT_OK){
					int iResult=data.getIntExtra(Constants.STR_RESULT, -1);
					String strError=data.getStringExtra(Constants.STR_ERROR);
					if(iResult==1){
						long lngUserId = data.getLongExtra(Constants.STR_USER_ID, 0L);
						if(lngUserId!=0L){
							context.getSupportFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
							context.getSupportFragmentManager().beginTransaction().replace(R.id.container, AddTripFragment.newInstance(lngUserId)).commit();
						} else{
							showMessage("Something went wrong!!");
						}
					} else if(iResult==3){
						showGMSNotFoundDialog();

					} else if(strError!=null){
						showMessage(strError);
					} else {
						showMessage("Registration was unsuccessful!!");
					}
					Log.d("Expense", "Error is "+strError);
					Log.d("Expense", "*************************Success***************************");
				} else{

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("Something went wrong!!");
		}
	}

	private void showGMSNotFoundDialog() {
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View view = getActivity().getLayoutInflater().inflate(R.layout.registration_error_dialog, null);
			builder.setCancelable(true);
			TextView textView = (TextView)view.findViewById(R.id.error);
			Button btnOk = (Button) view.findViewById(R.id.btnOk);
			btnOk.setText("Install");
			btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
					} catch (android.content.ActivityNotFoundException anfe) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms")));
					}
				}
			});

			textView.setText("Oops!! Seems like you don't have google play services installed. Please install and try again!!");

			alert = builder.create();
			alert.setView(view, 0, 0, 0, 0);
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		Handler handler=new Handler();
		if(animation.equals(bounceImage)){
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if(isVersionICSorGreater){
						exitAnim = AnimationUtils.loadAnimation(context, R.anim.down_n_hide);
						exitAnim.setInterpolator(new AnticipateOvershootInterpolator());
					} else{
						exitAnim=AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
					}
					exitAnim.setAnimationListener(LoginFragment.this);
					ivIcon.startAnimation(exitAnim);
				}
			}, 2000);
		} else if(animation.equals(exitAnim)){
			ivIcon.setVisibility(View.GONE);
			if(isVersionICSorGreater){
				enterAnim = AnimationUtils.loadAnimation(context, R.anim.down_n_bounce);
				enterAnim.setInterpolator(new BounceInterpolator());
			} else{
				enterAnim=AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
			}
			enterAnim.setAnimationListener(LoginFragment.this);
			svLogin.setVisibility(View.VISIBLE);
			svLogin.startAnimation(enterAnim);
			position=1;
		} else if(animation.equals(registerAnim)){
			svLogin.setVisibility(View.INVISIBLE);
			if(isVersionICSorGreater){
				regEnterAnim = AnimationUtils.loadAnimation(context, R.anim.down_n_bounce);
				regEnterAnim.setInterpolator(new BounceInterpolator());
			} else{
				regEnterAnim=AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
			}
			regEnterAnim.setAnimationListener(LoginFragment.this);
			svRegister.setVisibility(View.VISIBLE);
			svRegister.startAnimation(regEnterAnim);
			position=2;
		} else if(animation.equals(loginAnim)){
			svRegister.setVisibility(View.INVISIBLE);
			if(isVersionICSorGreater){
				enterAnim = AnimationUtils.loadAnimation(context, R.anim.down_n_bounce);
				enterAnim.setInterpolator(new BounceInterpolator());
			} else{
				enterAnim=AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
			}
			enterAnim.setAnimationListener(LoginFragment.this);
			svLogin.setVisibility(View.VISIBLE);
			svLogin.startAnimation(enterAnim);
			position=1;
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}
}