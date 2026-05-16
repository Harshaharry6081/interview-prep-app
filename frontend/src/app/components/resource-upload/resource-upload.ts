import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-resource-upload',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './resource-upload.html',
  styleUrl: './resource-upload.scss'
})
export class ResourceUpload {
  selectedFiles: File[] = [];
  isDragging = false;
  youtubeUrl = '';
  uploading = false;
  errorMsg = '';
  successMsg = '';

  // For demo: use a fixed folderId; in real usage this comes from the sidebar selection
  private activeFolderId = 'default-folder';
  private apiUrl = '/api/resources';

  constructor(private http: HttpClient, private auth: AuthService) {}

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragging = true;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragging = false;
    if (event.dataTransfer?.files) {
      this.addFiles(Array.from(event.dataTransfer.files));
    }
  }

  onFilesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.addFiles(Array.from(input.files));
    }
  }

  private addFiles(files: File[]) {
    const allowed = files.filter(f =>
      f.name.endsWith('.pdf') || f.name.endsWith('.txt') || f.name.endsWith('.md')
    );
    this.selectedFiles = [...this.selectedFiles, ...allowed];
  }

  uploadFiles() {
    if (!this.selectedFiles.length) return;
    this.uploading = true;
    this.errorMsg = '';
    this.successMsg = '';

    const formData = new FormData();
    this.selectedFiles.forEach(f => formData.append('files', f));
    formData.append('folderId', this.activeFolderId);

    const headers = new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
    this.http.post(`${this.apiUrl}/upload`, formData, { headers }).subscribe({
      next: () => {
        this.successMsg = `${this.selectedFiles.length} files uploaded and indexed for RAG!`;
        this.selectedFiles = [];
        this.uploading = false;
      },
      error: () => {
        this.errorMsg = 'Upload failed. Is the backend running?';
        this.uploading = false;
      }
    });
  }

  addYoutubeLink() {
    if (!this.youtubeUrl.trim()) return;
    const headers = new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
    this.http.post(`${this.apiUrl}/link`, {
      url: this.youtubeUrl,
      folderId: this.activeFolderId,
      title: 'YouTube Resource'
    }, { headers }).subscribe({
      next: () => {
        this.successMsg = 'YouTube link saved!';
        this.youtubeUrl = '';
      }
    });
  }
}
