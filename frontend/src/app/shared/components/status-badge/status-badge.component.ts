import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

type BadgeType = 'project' | 'task' | 'bug' | 'sprint' | 'requirement' | 'priority' | 'severity';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [NgClass, TranslateModule],
  templateUrl: './status-badge.component.html'
})
export class StatusBadgeComponent {
  @Input() status = '';
  @Input() type: BadgeType = 'task';

  get cssClass(): string {
    return `badge badge-${this.status.toLowerCase().replace(/_/g, '-')}`;
  }

  get translateKey(): string {
    switch (this.type) {
      case 'priority':
        return `PRIORITY.${this.status}`;
      case 'severity':
        return `SEVERITY.${this.status}`;
      default:
        return `STATUS.${this.status}`;
    }
  }
}
