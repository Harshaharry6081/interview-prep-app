import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth';
import { ProjectService, Project, Folder } from '../../services/project';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar implements OnInit {
  username = '';
  picture  = '';
  projects: Project[] = [];
  foldersByProject: Record<string, Folder[]> = {};
  expandedProjects: Record<string, boolean> = {};
  activeProjectId = '';
  activeFolderId = '';
  showNewProject = false;
  newProjectName = '';
  newFolderNames: Record<string, string> = {};

  constructor(private auth: AuthService, private projectService: ProjectService) {}

  ngOnInit() {
    this.username = this.auth.getUsername();
    this.picture  = this.auth.getPicture();
    this.loadProjects();
  }

  loadProjects() {
    this.projectService.getProjects().subscribe(projects => {
      this.projects = projects;
    });
  }

  toggleProject(projectId: string) {
    this.activeProjectId = projectId;
    this.expandedProjects[projectId] = !this.expandedProjects[projectId];
    if (this.expandedProjects[projectId] && !this.foldersByProject[projectId]) {
      this.projectService.getFolders(projectId).subscribe(folders => {
        this.foldersByProject[projectId] = folders;
      });
    }
  }

  createProject() {
    if (!this.newProjectName.trim()) return;
    this.projectService.createProject(this.newProjectName, '').subscribe(p => {
      this.projects.push(p);
      this.newProjectName = '';
      this.showNewProject = false;
    });
  }

  createFolder(projectId: string) {
    const name = this.newFolderNames[projectId];
    if (!name?.trim()) return;
    this.projectService.createFolder(name, projectId).subscribe(f => {
      if (!this.foldersByProject[projectId]) this.foldersByProject[projectId] = [];
      this.foldersByProject[projectId].push(f);
      this.newFolderNames[projectId] = '';
    });
  }

  selectFolder(folder: Folder) {
    this.activeFolderId = folder.id;
  }

  logout() {
    this.auth.logout();
  }
}
