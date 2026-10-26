import { Injectable, signal } from '@angular/core';

export const GOOGLE_CLIENT_ID = '416975871261-r9jj8ep4ir9mcsj76knq1l6h9rreoc15.apps.googleusercontent.com';
const GDRIVE_SCOPE = 'https://www.googleapis.com/auth/drive.appdata https://www.googleapis.com/auth/drive.file';

export interface GDriveUser {
  displayName: string;
  emailAddress: string;
  photoLink?: string;
}

export interface GDriveQuota {
  usedBytes: number;
  totalBytes: number;
  usedFormatted: string;
  totalFormatted: string;
  percent: number;
}

@Injectable({
  providedIn: 'root'
})
export class GDriveService {
  public isConnected = signal<boolean>(false);
  public currentUser = signal<GDriveUser | null>(null);
  public quota = signal<GDriveQuota>({
    usedBytes: 1258291,
    totalBytes: 16106127360,
    usedFormatted: '1.2 MB',
    totalFormatted: '15 GB',
    percent: 1
  });
  public isSyncing = signal<boolean>(false);
  public lastSyncTime = signal<string>('Never');

  private accessToken: string | null = null;

  constructor() {
    this.initToken();
  }

  private initToken() {
    // Check if token in URL hash after redirect
    if (typeof window !== 'undefined') {
      const hash = window.location.hash;
      if (hash && hash.includes('access_token=')) {
        const params = new URLSearchParams(hash.substring(1));
        const token = params.get('access_token');
        if (token) {
          this.setToken(token);
          // Clean the URL hash without reload
          window.history.replaceState(null, '', window.location.pathname);
        }
      } else {
        const saved = localStorage.getItem('studyflow-gdrive-token');
        if (saved) {
          this.accessToken = saved;
          this.isConnected.set(true);
          this.fetchDriveMetadata();
        }
      }
    }
  }

  setToken(token: string) {
    this.accessToken = token;
    localStorage.setItem('studyflow-gdrive-token', token);
    this.isConnected.set(true);
    this.fetchDriveMetadata();
  }

  disconnect() {
    this.accessToken = null;
    localStorage.removeItem('studyflow-gdrive-token');
    this.isConnected.set(false);
    this.currentUser.set(null);
  }

  startOAuthLogin() {
    if (typeof window === 'undefined') return;

    const redirectUri = window.location.origin + '/settings';
    const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?` +
      `client_id=${encodeURIComponent(GOOGLE_CLIENT_ID)}` +
      `&redirect_uri=${encodeURIComponent(redirectUri)}` +
      `&response_type=token` +
      `&scope=${encodeURIComponent(GDRIVE_SCOPE)}` +
      `&include_granted_scopes=true` +
      `&prompt=consent` +
      `&state=studyflow_gdrive_auth`;

    window.location.href = authUrl;
  }

  async fetchDriveMetadata() {
    if (!this.accessToken) return;

    try {
      const res = await fetch('https://www.googleapis.com/drive/v3/about?fields=user,storageQuota', {
        headers: { Authorization: `Bearer ${this.accessToken}` }
      });
      if (res.status === 401) {
        // Token expired
        this.disconnect();
        return;
      }
      const data = await res.json();
      if (data.user) {
        this.currentUser.set({
          displayName: data.user.displayName,
          emailAddress: data.user.emailAddress,
          photoLink: data.user.photoLink
        });
      }
      if (data.storageQuota) {
        const used = parseInt(data.storageQuota.usage || '0', 10);
        const limit = parseInt(data.storageQuota.limit || '16106127360', 10);
        this.quota.set({
          usedBytes: used,
          totalBytes: limit,
          usedFormatted: (used / (1024 * 1024)).toFixed(1) + ' MB',
          totalFormatted: (limit / (1024 * 1024 * 1024)).toFixed(0) + ' GB',
          percent: Math.min(100, Math.round((used / limit) * 100))
        });
      }
    } catch (e) {
      console.warn('Failed to fetch Google Drive quota', e);
    }
  }

  async uploadBackupToDrive(archiveData: any): Promise<boolean> {
    if (!this.accessToken) return false;
    this.isSyncing.set(true);

    try {
      const boundary = '-------314159265358979323846';
      const delimiter = "\r\n--" + boundary + "\r\n";
      const closeDelim = "\r\n--" + boundary + "--";

      const metadata = {
        name: `studyflow_vault_backup_${new Date().toISOString().split('T')[0]}.json`,
        mimeType: 'application/json',
        parents: ['appDataFolder'] // Secure hidden folder specific to StudyFlow
      };

      const multipartRequestBody =
        delimiter +
        'Content-Type: application/json\r\n\r\n' +
        JSON.stringify(metadata) +
        delimiter +
        'Content-Type: application/json\r\n\r\n' +
        JSON.stringify(archiveData) +
        closeDelim;

      const res = await fetch('https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${this.accessToken}`,
          'Content-Type': `multipart/related; boundary=${boundary}`
        },
        body: multipartRequestBody
      });

      if (res.ok) {
        this.lastSyncTime.set(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
        return true;
      }
      return false;
    } catch (e) {
      console.error('Drive backup upload error', e);
      return false;
    } finally {
      this.isSyncing.set(false);
    }
  }
}
