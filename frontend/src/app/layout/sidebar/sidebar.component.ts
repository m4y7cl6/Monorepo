import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatListModule, MatIconModule, TranslateModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  readonly navItems: NavItem[] = [
    { label: 'NAV.PROJECTS', icon: 'folder', route: '/projects' },
    { label: 'NAV.TASKS', icon: 'assignment', route: '/tasks' },
    { label: 'NAV.BUGS', icon: 'bug_report', route: '/bugs' },
    { label: 'NAV.REQUIREMENTS', icon: 'list_alt', route: '/requirements' },
    { label: 'NAV.WORKLOGS', icon: 'schedule', route: '/worklogs' }
  ];
}
