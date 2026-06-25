import { Component, EventEmitter, OnInit, Output, inject, signal } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    TranslateModule
  ],
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.scss']
})
export class TopbarComponent implements OnInit {
  @Output() menuToggle = new EventEmitter<void>();

  private readonly keycloak = inject(KeycloakService);
  private readonly translate = inject(TranslateService);

  readonly userName = signal<string>('');
  readonly userEmail = signal<string>('');
  readonly currentLang = signal<string>('zh-TW');

  ngOnInit(): void {
    const profile = this.keycloak.getKeycloakInstance().tokenParsed;
    if (profile) {
      const fullName = `${profile['given_name'] ?? ''} ${profile['family_name'] ?? ''}`.trim();
      this.userName.set(fullName || profile['preferred_username'] || '');
      this.userEmail.set(profile['email'] ?? '');
    }
    const savedLang = localStorage.getItem('lang') ?? 'zh-TW';
    this.currentLang.set(savedLang);
  }

  toggleLang(): void {
    const newLang = this.currentLang() === 'zh-TW' ? 'en' : 'zh-TW';
    this.currentLang.set(newLang);
    this.translate.use(newLang);
    localStorage.setItem('lang', newLang);
  }

  logout(): void {
    this.keycloak.logout(window.location.origin);
  }

  onMenuToggle(): void {
    this.menuToggle.emit();
  }
}
