import { Strategy, ExtractJwt } from 'passport-jwt';
import { PassportStrategy } from '@nestjs/passport';
import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config'
import { User } from 'src/app.entities';
import { UsersService } from './users/users.service';

@Injectable()
export class JwtAuthStrategy extends PassportStrategy(Strategy) {
  constructor(private userService: UsersService, configService: ConfigService) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      secretOrKey: configService.getOrThrow('JWT_SECRET')
    });
  }

  async validate(jwtPayload: object & {sub: string}): Promise<User> {
    try {
      const user = await this.userService.validateJWT(jwtPayload);
      if (!user) {
        throw new UnauthorizedException();
      }
      return user;
    } catch (e: unknown) {
      throw new UnauthorizedException();
    }
  }
}