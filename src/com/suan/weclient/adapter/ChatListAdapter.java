package com.suan.weclient.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.suan.weclient.R;
import com.suan.weclient.activity.ShowImgActivity;
import com.suan.weclient.util.ListCacheManager;
import com.suan.weclient.util.Util;
import com.suan.weclient.util.data.DataManager;
import com.suan.weclient.util.data.bean.MessageBean;
import com.suan.weclient.util.net.WeChatLoader;
import com.suan.weclient.util.net.WechatManager;
import com.suan.weclient.util.net.WechatManager.OnActionFinishListener;
import com.suan.weclient.util.net.images.ImageCacheManager;
import com.suan.weclient.util.text.SpanUtil;
import com.suan.weclient.util.voice.VoiceHolder;
import com.suan.weclient.util.voice.VoiceManager.AudioPlayListener;

public class ChatListAdapter extends BaseAdapter implements OnScrollListener {
    private LayoutInflater mInflater;
    private ListCacheManager mListCacheManager;
    private DataManager mDataManager;
    private Context mContext;

    private HashMap<String, VoiceHolder> voiceCache;

    /*
     * whether the scroll is busy
     */
    private boolean mBusy = false;

    public ChatListAdapter(Context context, DataManager dataManager) {
        this.mInflater = LayoutInflater.from(context);
        this.mDataManager = dataManager;
        this.mContext = context;
        this.mListCacheManager = new ListCacheManager();
        this.voiceCache = new HashMap<String, VoiceHolder>();
        initListener();
    }

    private void initListener() {
        mDataManager.addChatNewItemGetListener(new DataManager.ChatNewItemGetListener() {
            @Override
            public void onChatItemGet(ArrayList<MessageBean> getMessage, String msgId) {
                int index = -1;
                for (int i = 0; i < getMessageItems().size(); i++) {
                    MessageBean nowMessage = getMessageItems().get(i);
                    if (nowMessage.getId().equals(msgId)) {
                        index = i;

                    }
                }
                if (index != -1) {
                    getMessageItems().remove(index);
                    for (int i = 0; i < getMessage.size(); i++) {
                        getMessageItems().add(index, getMessage.get(i));
                    }

                }

            }
        });

    }

    private ArrayList<MessageBean> getMessageItems() {
        if (mDataManager.getChatHolder() == null) {
            ArrayList<MessageBean> blankArrayList = new ArrayList<MessageBean>();
            return blankArrayList;
        }
        return mDataManager.getChatHolder().getMessageList();
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return getMessageItems().size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    public void updateCache() {
        mListCacheManager.clearData();

    }

    public View newView(final int position) {

        View convertView = null;
        switch (getMessageItems().get(position).getType()
                + getMessageItems().get(position).getOwner() * 10) {
            case MessageBean.MESSAGE_OWNER_HER * 10 + MessageBean.MESSAGE_TYPE_TEXT:
                convertView = mInflater.inflate(R.layout.chat_item_her_text_layout,
                        null);
                break;

            case MessageBean.MESSAGE_OWNER_HER * 10 + MessageBean.MESSAGE_TYPE_IMG:
                convertView = mInflater.inflate(R.layout.chat_item_her_img_layout,
                        null);
                break;
            case MessageBean.MESSAGE_OWNER_HER * 10
                    + MessageBean.MESSAGE_TYPE_VOICE:

                convertView = mInflater.inflate(
                        R.layout.chat_item_her_voice_layout, null);

                break;

            case MessageBean.MESSAGE_OWNER_ME * 10 + MessageBean.MESSAGE_TYPE_TEXT:
                convertView = mInflater.inflate(R.layout.chat_item_me_text_layout,
                        null);
                break;

            case MessageBean.MESSAGE_OWNER_ME * 10 + MessageBean.MESSAGE_TYPE_IMG:
                convertView = mInflater.inflate(R.layout.chat_item_me_img_layout,
                        null);
                break;
            case MessageBean.MESSAGE_OWNER_ME * 10 + MessageBean.MESSAGE_TYPE_VOICE:

                convertView = mInflater.inflate(R.layout.chat_item_me_voice_layout,
                        null);

                break;
            default:

                if (getMessageItems().get(position).getOwner() == MessageBean.MESSAGE_OWNER_ME) {

                    convertView = mInflater.inflate(R.layout.chat_item_me_text_layout,

                            null);
                } else {

                    convertView = mInflater.inflate(R.layout.chat_item_her_text_layout,

                            null);
                }
                break;

        }

/*

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int messageSendState = getMessageItems().get(position).getSendState();
                switch (messageSendState) {
                    case MessageBean.MESSAGE_SEND_FAILED:

                        break;

                    case MessageBean.MESSAGE_SEND_NONE:

                        break;
                    case MessageBean.MESSAGE_SEND_OK:

                        break;
                }

                return false;
            }
        });
*/

        return convertView;

    }

    private ItemViewHolder getHolder(final View view, int position) {

        ItemViewHolder holder = (ItemViewHolder) view.getTag();
        if (holder == null) {
            holder = new ItemViewHolder(view, position);
            view.setTag(holder);
        }
        return holder;
    }

    public class ItemViewHolder {

        private ImageView profileImageView;
        private TextView timeTextView;

        private RelativeLayout contentLayout;
        private ImageView contentImageView;
        private TextView contentTextView;

        private ImageView voicePlayView;
        private TextView voiceInfoTextView;

        public ItemViewHolder(View parentView, final int position) {

            switch (getMessageItems().get(position).getType()
                    + getMessageItems().get(position).getOwner() * 10) {
                case MessageBean.MESSAGE_OWNER_HER * 10
                        + MessageBean.MESSAGE_TYPE_TEXT:

                    contentLayout = (RelativeLayout) parentView.findViewById(R.id.chat_item_her_text_layout_content);
                    contentTextView = (TextView) parentView
                            .findViewById(R.id.chat_item_her_text_text_content);
                    profileImageView = (ImageView) parentView
                            .findViewById(R.id.chat_item_her_text_img_profile);
                    timeTextView = (TextView) parentView
                            .findViewById(R.id.chat_item_her_text_text_time);
                    String content = getMessageItems().get(position)
                            .getContent();

                    SpanUtil.setHtmlSpanAndImgSpan(contentTextView, content, mContext);


                    break;

                case MessageBean.MESSAGE_OWNER_HER * 10
                        + MessageBean.MESSAGE_TYPE_IMG:

                    contentLayout = (RelativeLayout) parentView.findViewById(R.id.chat_item_her_img_layout_content);
                    contentImageView = (ImageView) parentView
                            .findViewById(R.id.chat_item_her_img_img_content);
                    profileImageView = (ImageView) parentView
                            .findViewById(R.id.chat_item_her_img_img_profile);
                    timeTextView = (TextView) parentView
                            .findViewById(R.id.chat_item_her_img_text_time);

                    break;

                case MessageBean.MESSAGE_OWNER_HER * 10
                        + MessageBean.MESSAGE_TYPE_VOICE:

                    contentLayout = (RelativeLayout) parentView.findViewById(R.id.chat_item_her_voice_layout_content);
                    profileImageView = (ImageView) parentView
                            .findViewById(R.id.chat_item_her_voice_img_profile);
                    timeTextView = (TextView) parentView
                            .findViewById(R.id.chat_item_her_voice_text_time);
                    voicePlayView = (ImageView) parentView.findViewById(R.id.chat_item_her_voice_button_play);
                    voiceInfoTextView = (TextView) parentView.findViewById(R.id.chat_item_her_voice_text_info);
                    break;

                case MessageBean.MESSAGE_OWNER_ME * 10
                        + MessageBean.MESSAGE_TYPE_TEXT:

                    contentLayout = (RelativeLayout) parentView.findViewById(R.id.chat_item_me_text_layout_content);
                    contentTextView = (TextView) parentView
                            .findViewById(R.id.chat_item_me_text_text_content);
                    profileImageView = (ImageView) parentView
                            .findViewById(R.id.chat_item_me_text_img_profile);
                    timeTextView = (TextView) parentView
                            .findViewById(R.id.chat_item_me_text_text_time);
                    String meContent = getMessageItems().get(position)
                            .getContent();

                    SpanUtil.setHtmlSpanAndImgSpan(contentTextView, meContent, mContext);


                    break;

                case MessageBean.MESSAGE_OWNER_ME * 10
                        + MessageBean.MESSAGE_TYPE_IMG:

                    contentLayout = (RelativeLayout) parentView.findViewById(R.id.chat_item_me_img_layout_content);
                    contentImageView = (ImageView) parentView
                            .findViewById(R.id.chat_item_me_img_img_content);
                    profileImageView = (ImageView) parentView
                            .findViewById(R.id.chat_item_me_img_img_profile);
                    timeTextView = (TextView) parentView
                            .findViewById(R.id.chat_item_me_img_text_time);

                    break;

                case MessageBean.MESSAGE_OWNER_ME * 10
                        + MessageBean.MESSAGE_TYPE_VOICE:

                    contentLayout = (RelativeLayout) parentView.findViewById(R.id.chat_item_me_voice_layout_content);
                    profileImageView = (ImageView) parentView
                            .findViewById(R.id.chat_item_me_voice_img_profile);
                    timeTextView = (TextView) parentView
                            .findViewById(R.id.chat_item_me_voice_text_time);
                    voicePlayView = (ImageView) parentView.findViewById(R.id.chat_item_me_voice_button_play);
                    voiceInfoTextView = (TextView) parentView.findViewById(R.id.chat_item_me_voice_text_info);

                    break;

                default:
                    if (getMessageItems().get(position).getOwner() == MessageBean.MESSAGE_OWNER_ME) {

                        contentLayout = (RelativeLayout) parentView.findViewById(R.id.chat_item_me_text_layout_content);
                        contentTextView = (TextView) parentView
                                .findViewById(R.id.chat_item_me_text_text_content);
                        profileImageView = (ImageView) parentView
                                .findViewById(R.id.chat_item_me_text_img_profile);
                        timeTextView = (TextView) parentView
                                .findViewById(R.id.chat_item_me_text_text_time);
                        contentTextView.setText("[目前暂不支持该类型消息]");

                    } else {

                        contentLayout = (RelativeLayout) parentView.findViewById(R.id.chat_item_her_text_layout_content);
                        contentTextView = (TextView) parentView
                                .findViewById(R.id.chat_item_her_text_text_content);
                        profileImageView = (ImageView) parentView
                                .findViewById(R.id.chat_item_her_text_img_profile);
                        timeTextView = (TextView) parentView
                                .findViewById(R.id.chat_item_her_text_text_time);
                        contentTextView.setText("[目前暂不支持该类型消息]");
                    }

                    break;

            }
        }

    }

    public void bindView(View view, final int position) {

        ItemViewHolder holder = getHolder(view, position);

        switch (getMessageItems().get(position).getType()) {
            case MessageBean.MESSAGE_TYPE_TEXT:

                break;

            case MessageBean.MESSAGE_TYPE_IMG:

                setImgMessageContent(holder, position);

                break;

            case MessageBean.MESSAGE_TYPE_VOICE:

                setVoiceMessageContent(holder, position);

                break;

            default:

                break;

        }

        long time = Long.parseLong(getMessageItems().get(position)
                .getDateTime());
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("MM.dd HH:mm ");
        String timeString = "" + format.format(date);

        holder.timeTextView.setText(timeString);

        if (getMessageItems().get(position).getSendState() >= MessageBean.MESSAGE_SEND_PREPARE && getMessageItems().get(position).getMessageSendListener() == null) {

            switch (getMessageItems().get(position).getSendState()) {
                case MessageBean.MESSAGE_SEND_ING:
                    holder.contentLayout.setSelected(true);

                    break;
                case MessageBean.MESSAGE_SEND_OK:
                    holder.contentLayout.setSelected(false);

                    break;
                default:
                    holder.contentLayout.setSelected(true);

                    break;
            }


            getMessageItems().get(position).setMessageSendListener(holder, new MessageBean.MessageSendListener() {
                @Override
                public void onSendFinish(int state, ItemViewHolder holder) {
                    switch (state) {
                        case MessageBean.MESSAGE_SEND_ING:
                            holder.contentLayout.setSelected(true);

                            break;
                        case MessageBean.MESSAGE_SEND_OK:
                            holder.contentLayout.setSelected(false);

                            break;
                        default:
                            holder.contentLayout.setSelected(true);

                            break;
                    }

                }
            });
        }
        if (getMessageItems().get(position).getSendState() == MessageBean.MESSAGE_SEND_FAILED_FANS_NOT_RECEIVE||
                getMessageItems().get(position).getSendState()==MessageBean.MESSAGE_SEND_FAILED_LIMIT_OF_TIME) {
            holder.contentLayout.setSelected(true);

        }


        setHeadImg(holder, position);

    }

    private void setVoiceMessageContent(final ItemViewHolder holder,
                                        final int position) {

        boolean voiceLoaded = false;
        if (holder.contentLayout.getTag() != null) {
            voiceLoaded = true;
        }

        if (!mBusy || !voiceLoaded) {

            if (voiceCache.containsKey(getMessageId(position))) {
                VoiceHolder voiceHolder = voiceCache.get(getMessageId(position));

                int playLength = Integer.parseInt(getMessageItems().get(position).getPlayLength());
                int seconds = playLength / 1000;
                int minutes = seconds / 60;
                int leaveSecond = seconds % 60;
                String info = "";
                if (minutes != 0) {
                    info += minutes + "'";

                }
                info += " " + leaveSecond + "'";
                holder.voiceInfoTextView.setText(info);
                holder.contentLayout.setTag(voiceHolder);
            } else {

                mDataManager.getWechatManager().getMessageVoice(
                        mDataManager.getCurrentPosition(),
                        getMessageItems().get(position),
                        Integer.parseInt(getMessageItems().get(position)
                                .getLength()), mDataManager.getCurrentUser(),
                        new OnActionFinishListener() {

                            @Override
                            public void onFinish(int code, Object object) {
                                // TODO Auto-generated method stub
                                try {

                                    byte[] bytes = (byte[]) object;
                                    VoiceHolder voiceHolder = new VoiceHolder(
                                            bytes, getMessageItems().get(position)
                                            .getPlayLength(),
                                            getMessageItems().get(position)
                                                    .getLength());
                                    int playLength = Integer.parseInt(getMessageItems().get(position).getPlayLength());
                                    int seconds = playLength / 1000;
                                    int minutes = seconds / 60;
                                    int leaveSecond = seconds % 60;
                                    String info = "";
                                    if (minutes != 0) {
                                        info += minutes + "'";

                                    }
                                    info += " " + leaveSecond + "'";
                                    holder.voiceInfoTextView.setText(info);

                                    holder.contentLayout.setTag(voiceHolder);
                                    voiceCache.put(getMessageId(position),voiceHolder);


                                } catch (Exception exception) {

                                }

                            }
                        });

            }


        }
        holder.contentLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (holder.contentLayout.getTag() != null) {

                    final VoiceHolder voiceHolder = (VoiceHolder) holder.contentLayout
                            .getTag();
                    if (voiceHolder.getPlaying()) {
                        mDataManager.getVoiceManager().stopMusic();

                    } else {

                        mDataManager.getVoiceManager().playVoice(
                                voiceHolder.getBytes(),
                                voiceHolder.getPlayLength(),
                                voiceHolder.getLength(),
                                new AudioPlayListener() {

                                    @Override
                                    public void onAudioStop() {
                                        // TODO Auto-generated method stub

                                        voiceHolder.setPlaying(false);
                                        holder.voicePlayView.setSelected(false);

                                    }

                                    @Override
                                    public void onAudioStart() {
                                        // TODO Auto-generated method stub
                                        voiceHolder.setPlaying(true);
                                        holder.voicePlayView.setSelected(true);

                                    }

                                    @Override
                                    public void onAudioError() {
                                        // TODO Auto-generated method stub

                                    }
                                });
                    }

                } else {

                }

            }
        });
    }

    private void setImgMessageContent(final ItemViewHolder holder,
                                      final int position) {

        holder.contentImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDataManager.createImgHolder(getMessageItems().get(position), mDataManager.getCurrentUser());
                Intent jumbIntent = new Intent();
                jumbIntent.setClass(mContext, ShowImgActivity.class);
                mContext.startActivity(jumbIntent);

            }
        });
        boolean imgLoaded = false;
        if (holder.contentImageView.getTag() != null) {
            imgLoaded = (Boolean) holder.contentImageView.getTag();
        }

        if (!mBusy || !imgLoaded) {


            Bitmap contentBitmap = mDataManager.getCacheManager().getBitmap(
                    ImageCacheManager.CACHE_MESSAGE_CONTENT
                            + getMessageItems().get(position).getId());
            if (contentBitmap == null) {
                mDataManager.getWechatManager().getMessageImg(
                        mDataManager.getCurrentPosition(),
                        getMessageItems().get(position),
                        holder.contentImageView,
                        WeChatLoader.WECHAT_URL_MESSAGE_IMG_SMALL,
                        new OnActionFinishListener() {

                            @Override
                            public void onFinish(int code, Object object) {
                                // TODO Auto-generated method stub
                                if (code == WechatManager.ACTION_SUCCESS) {
                                    holder.contentImageView.setTag(true);
                                    Bitmap bitmap = (Bitmap) object;
                                    mDataManager.getCacheManager().putBitmap(
                                            ImageCacheManager.CACHE_MESSAGE_CONTENT
                                                    + getMessageItems().get(
                                                    position).getId(),
                                            bitmap, true);

                                }

                            }
                        });

            } else {
                holder.contentImageView.setImageBitmap(contentBitmap);
            }
        }
    }

    private void setHeadImg(final ItemViewHolder holder, final int position) {

        boolean imgLoaded = false;
        if (holder.profileImageView.getTag() != null) {
            imgLoaded = (Boolean) holder.profileImageView.getTag();
        }

        if (!mBusy || !imgLoaded) {

            Bitmap headBitmap = mDataManager.getCacheManager().getBitmap(
                    ImageCacheManager.CACHE_CHAT_LIST_PROFILE
                            + getMessageItems().get(position).getFakeId());
            if (headBitmap != null) {


                if (getMessageItems().get(position).getOwner() == MessageBean.MESSAGE_OWNER_ME) {

                    Bitmap bitmap = Util.roundCornerWithBorder(headBitmap,
                            (int) Util.dipToPx(40, mContext.getResources()), 15,
                            Color.parseColor("#ffffff"));

                    holder.profileImageView.setImageBitmap(bitmap);

                } else {

                    holder.profileImageView.setImageBitmap(headBitmap);
                }


            } else {
                mDataManager.getWechatManager().getMessageHeadImg(
                        mDataManager.getCurrentPosition(),
                        getMessageItems().get(position).getFakeId(),
                        getMessageItems().get(position).getReferer(),
                        holder.profileImageView, new OnActionFinishListener() {

                    @Override
                    public void onFinish(int code, Object object) {
                        // TODO Auto-generated method stub
                        if (code == WechatManager.ACTION_SUCCESS) {
                            holder.profileImageView.setTag(true);
                            Bitmap roundBitmap = Util.roundCornerWithBorder((Bitmap) object,
                                    (int) Util.dipToPx(40, mContext.getResources()), 15, Color.parseColor("#ffffff"));

                            holder.profileImageView.setImageBitmap(roundBitmap);

                            mDataManager.getCacheManager().putBitmap(
                                    ImageCacheManager.CACHE_CHAT_LIST_PROFILE
                                            + getMessageItems().get(
                                            position).getFakeId(),
                                    roundBitmap, true);

                        }

                    }
                });

            }
        }
    }

    private String getMessageId(int position) {
        return getMessageItems().get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v;
        v = newView(position);
        bindView(v, position);

        return v;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) { // 滑动停止
            mBusy = false;

        } else if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {// 滑动手未松开
            mBusy = true;
        } else if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {// 滑动中手已松开
            mBusy = true;
        }
    }

}