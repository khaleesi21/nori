package com.vomitcuddle.norilib;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.volley.RequestQueue;
import com.vomitcuddle.norilib.clients.Danbooru;
import com.vomitcuddle.norilib.clients.DanbooruLegacy;
import com.vomitcuddle.norilib.clients.Gelbooru;
import com.vomitcuddle.norilib.clients.Imageboard;
import com.vomitcuddle.norilib.clients.Shimmie2;

public class ServiceSettings implements Parcelable {
  /** Class loader used by unpacking Parcelables. */
  public static final Creator<ServiceSettings> CREATOR = new Creator<ServiceSettings>() {
    @Override
    public ServiceSettings createFromParcel(Parcel source) {
      return new ServiceSettings(source);
    }

    @Override
    public ServiceSettings[] newArray(int size) {
      return new ServiceSettings[size];
    }
  };
  /** URL to the service's API endpoint. */
  public String apiEndpoint;
  /** API version/type. */
  public ServiceType apiType;
  /** API Username */
  public String username;
  /** API Password */
  public String password;

  /** Default constructor */
  public ServiceSettings() {
  }

  /**
   * Constructor used by {@link com.vomitcuddle.norilib.clients.Imageboard#exportServiceSettings()}.
   *
   * @param apiEndpoint API endpoint URL.
   * @param apiType     API version/type.
   * @param username    API username.
   * @param password    API password.
   */
  public ServiceSettings(String apiEndpoint, ServiceType apiType, String username, String password) {
    this.apiEndpoint = apiEndpoint;
    this.apiType = apiType;
    this.username = username;
    this.password = password;
  }

  /**
   * Constructor used for deserializing from parcels.
   *
   * @param in Parcel to read data from.
   */
  public ServiceSettings(Parcel in) {
    this.apiEndpoint = in.readString();
    this.apiType = (ServiceType) in.readSerializable();
    if (in.readByte() == 0x01)
      this.username = in.readString();
    if (in.readByte() == 0x01)
      this.password = in.readString();
  }

  /**
   * Creates a new Imageboard based on the {@link com.vomitcuddle.norilib.ServiceSettings}.
   *
   * @param requestQueue Volley {@link com.android.volley.RequestQueue}.
   * @return Imageboard client with current settings.
   */
  public Imageboard createClient(RequestQueue requestQueue) {
    switch (apiType) {
      case DANBOORU:
        if (username != null && password != null)
          return new Danbooru(apiEndpoint, requestQueue, username, password);
        return new Danbooru(apiEndpoint, requestQueue);
      case DANBOORU_LEGACY:
        if (username != null && password != null)
          return new DanbooruLegacy(apiEndpoint, requestQueue, username, password);
        return new DanbooruLegacy(apiEndpoint, requestQueue);
      case GELBOORU:
        if (username != null && password != null)
          return new Gelbooru(apiEndpoint, requestQueue, username, password);
        return new Gelbooru(apiEndpoint, requestQueue);
      case SHIMMIE2:
        if (username != null && password != null)
          return new Shimmie2(apiEndpoint, requestQueue, username, password);
        return new Shimmie2(apiEndpoint, requestQueue);
      default:
        return null;
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(apiEndpoint);
    dest.writeSerializable(apiType);
    // Write 0x01 if username isn't null.
    dest.writeByte((byte) (username != null ? 0x01 : 0x00));
    if (username != null)
      dest.writeString(username);
    // Write 0x01 if password isn't null.
    dest.writeByte((byte) (password != null ? 0x01 : 0x00));
    if (password != null)
      dest.writeString(password);
  }

  /** API types */
  public enum ServiceType {
    DANBOORU,
    DANBOORU_LEGACY,
    GELBOORU,
    SHIMMIE2
  }
}
