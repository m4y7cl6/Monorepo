import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />'
})
export class AppComponent implements OnInit {
  private readonly translate = inject(TranslateService);

  ngOnInit(): void {
    const savedLang = localStorage.getItem('lang') ?? 'zh-TW';
    this.translate.addLangs(['zh-TW', 'en']);
    this.translate.setDefaultLang('zh-TW');
    this.translate.use(savedLang);
  }
}
