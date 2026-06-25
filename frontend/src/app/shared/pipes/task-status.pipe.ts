import { Pipe, PipeTransform } from '@angular/core';
import { TaskStatus } from '../../core/models/task.model';

@Pipe({
  name: 'taskStatus',
  standalone: true
})
export class TaskStatusPipe implements PipeTransform {
  transform(value: TaskStatus): string {
    const map: Record<TaskStatus, string> = {
      BACKLOG: 'STATUS.BACKLOG',
      TODO: 'STATUS.TODO',
      IN_PROGRESS: 'STATUS.IN_PROGRESS',
      IN_REVIEW: 'STATUS.IN_REVIEW',
      DONE: 'STATUS.DONE',
      CANCELLED: 'STATUS.CANCELLED'
    };
    return map[value] ?? value;
  }
}
