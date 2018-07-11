package com.hapramp.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hapramp.R;
import com.hapramp.api.RetrofitServiceGenerator;
import com.hapramp.preferences.HaprampPreferenceManager;
import com.hapramp.steem.models.user.SteemUser;
import com.hapramp.ui.activity.AccountHistoryActivity;
import com.hapramp.ui.adapters.AccountHistoryAdapter;
import com.hapramp.utils.ImageHandler;
import com.hapramp.views.extraa.BubbleProgressBar;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hapramp.walletinfo.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EarningFragment extends Fragment implements Wallet.UserAccountFieldsCallback {
		@BindView(R.id.wallet_steem_tv)
		TextView walletSteemTv;
		@BindView(R.id.wallet_steem_power_tv)
		TextView walletSteemPowerTv;
		@BindView(R.id.wallet_steem_dollar_tv)
		TextView walletSteemDollarTv;
		@BindView(R.id.wallet_saving_tv)
		TextView walletSavingTv;
		@BindView(R.id.wallet_est_account_value_tv)
		TextView walletEstAccountValueTv;
		@BindView(R.id.steem_progress)
		BubbleProgressBar steemProgress;
		@BindView(R.id.steem_power_progress)
		BubbleProgressBar steemPowerProgress;
		@BindView(R.id.steem_dollar_progress)
		BubbleProgressBar steemDollarProgress;
		@BindView(R.id.saving_progress)
		BubbleProgressBar savingProgress;
		@BindView(R.id.estimated_value_progress)
		BubbleProgressBar estimatedValueProgress;
		@BindView(R.id.user_image)
		ImageView userImage;
		@BindView(R.id.username)
		TextView username;
		@BindView(R.id.user_reputation)
		TextView userReputation;
		@BindView(R.id.steem_rate)
		TextView steemRate;
		@BindView(R.id.steem_dollar_rate)
		TextView steemDollarRate;
		@BindView(R.id.user_fullname)
		TextView userFullname;
		@BindView(R.id.steem_power_icon)
		ImageView steemPowerIcon;
		@BindView(R.id.see_history_btn)
		TextView seeHistoryBtn;

		private Handler mHandler;
		private Wallet wallet;
		private Unbinder unbinder;
		private String mUsername;
		private Context mContext;
		public static final String ARG_USERNAME = "username";

		public EarningFragment() {
				mHandler = new Handler();
				wallet = new Wallet();
				wallet.setUserAccountFieldsCallback(this);
		}


		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState) {
				View view = inflater.inflate(R.layout.fragment_earning, container, false);
				if (getArguments() != null) {
						mUsername = getArguments().getString(ARG_USERNAME);
				} else {
						mUsername = HaprampPreferenceManager.getInstance().getCurrentSteemUsername();
				}
				unbinder = ButterKnife.bind(this, view);
				fetchUserInfo();
				fetchWalletInfo();
				attachListener();
				return view;
		}

		@Override
		public void onAttach(Context context) {
				this.mContext = context;
				super.onAttach(context);
		}

		@Override
		public void onDestroyView() {
				super.onDestroyView();
				unbinder.unbind();
		}

		private void attachListener() {
				seeHistoryBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
								Intent intent = new Intent(mContext, AccountHistoryActivity.class);
								intent.putExtra(AccountHistoryActivity.EXTRA_USERNAME, mUsername);
								mContext.startActivity(intent);
						}
				});
		}

		private void fetchUserInfo() {
				String current_user_api_url = String.format(mContext.getResources().getString(R.string.steem_user_api),
						mUsername);
				RetrofitServiceGenerator.getService()
						.getSteemUser(current_user_api_url)
						.enqueue(new Callback<SteemUser>() {
								@Override
								public void onResponse(Call<SteemUser> call, Response<SteemUser> response) {
										if (response.isSuccessful()) {
												bindData(response.body());
										}
								}

								@Override
								public void onFailure(Call<SteemUser> call, Throwable t) {
								}
						});
		}

		private void fetchWalletInfo() {
				wallet.requestUserAccount(mUsername);
		}

		private void bindData(SteemUser data) {
				try {
						if (data != null) {
								username.setText(mUsername);
								userFullname.setText(data.user.getJsonMetadata().profile.name);
								long rawReputation = data.user.reputation;
								userReputation.setText(String.format(Locale.US, "(%.2f)", calculateReputation(rawReputation)));
								String profile_pic = String.format(getResources().getString(R.string.steem_user_profile_pic_format_large), mUsername);
								ImageHandler.loadCircularImage(mContext, userImage, profile_pic);
						}
				}
				catch (Exception e) {

				}
		}

		private double calculateReputation(long raw) {
				return (Math.log10(raw) - 9) * 9 + 25;
		}

		@Override
		public void onUserSteem(final String steem) {
				mHandler.post(new Runnable() {
						@Override
						public void run() {
								try {
										steemProgress.setVisibility(View.GONE);
										walletSteemTv.setText(steem);
								}
								catch (Exception e) {
								}
						}
				});
		}

		@Override
		public void onUserSteemDollar(final String dollar) {
				mHandler.post(new Runnable() {
						@Override
						public void run() {
								try {
										steemDollarProgress.setVisibility(View.GONE);
										walletSteemDollarTv.setText(dollar);
								}
								catch (Exception e) {
								}
						}
				});
		}

		@Override
		public void onUserSteemPower(final String steemPower) {
				mHandler.post(new Runnable() {
						@Override
						public void run() {
								try {
										steemPowerProgress.setVisibility(View.GONE);
										walletSteemPowerTv.setText(steemPower);
								}
								catch (Exception e) {
								}
						}
				});
		}

		@Override
		public void onUserSavingSteem(final String savingSteem) {
				mHandler.post(new Runnable() {
						@Override
						public void run() {
								try {
										savingProgress.setVisibility(View.GONE);
										walletSavingTv.append(savingSteem + ", ");
								}
								catch (Exception e) {
								}
						}
				});
		}

		@Override
		public void onUserSavingSBD(final String savingSBD) {
				mHandler.post(new Runnable() {
						@Override
						public void run() {
								try {
										savingProgress.setVisibility(View.GONE);
										walletSavingTv.append(savingSBD + " ");
								}
								catch (Exception e) {
								}
						}
				});
		}

		@Override
		public void onUserEstimatedAccountValue(final String value) {
				mHandler.post(new Runnable() {
						@Override
						public void run() {
								try {
										estimatedValueProgress.setVisibility(View.GONE);
										walletEstAccountValueTv.setText(value);
								}
								catch (Exception e) {
								}
						}
				});
		}

		@Override
		public void onUsdRates(final double sbd_usd, final double steem_usd) {
				mHandler.post(new Runnable() {
						@Override
						public void run() {
								try {
										steemDollarRate.setText(String.format(" $ %s", sbd_usd));
										steemRate.setText(String.format(" $ %s", steem_usd));
								}
								catch (Exception e) {
								}
						}
				});
		}

		@Override
		public void onError(String error) {

		}
}
